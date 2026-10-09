from contextlib import asynccontextmanager
from collections.abc import AsyncIterator
from fastapi import FastAPI
from fastapi.testclient import TestClient
from app.api.reports import router
from app.clients.spring_api import SpringApiError
from app.models.spring import AnalysisData


class FakeClient:
    async def get_analysis_data(self, user_id: int, from_date: str, to_date: str,
                                request_id: str, authorization: str) -> AnalysisData:
        assert authorization == "Bearer unit-test-token"
        return AnalysisData(user_id=user_id, sessions=[], records=[], matches=[])


def create_client(fake: object = FakeClient()) -> TestClient:
    @asynccontextmanager
    async def lifespan(app: FastAPI) -> AsyncIterator[None]:
        app.state.spring_api_client = fake
        yield
    app = FastAPI(lifespan=lifespan)
    app.include_router(router)
    return TestClient(app, headers={"Authorization": "Bearer unit-test-token"})


def test_未ログインでは分析データを要求しない() -> None:
    with create_client() as client:
        response = client.get("/api/analysis/users/1/training-report?from=2026-08-01&to=2026-08-31",
                              headers={"Authorization": ""})
    assert response.status_code == 401


def test_正常時は空データでも構造を維持する() -> None:
    with create_client() as client:
        response = client.get("/api/analysis/users/1/training-report?from=2026-08-01&to=2026-08-31")
    assert response.status_code == 200
    assert response.json()["frequency"]["sessionCount"] == 0
    assert response.json()["weightProgress"] == []
    assert "completeness" not in response.json()


def test_開始日と終了日が同じでも動作する() -> None:
    with create_client() as client:
        response = client.get("/api/analysis/users/1/training-report?from=2028-02-29&to=2028-02-29")
    assert response.status_code == 200
    assert response.json()["period"]["days"] == 1


def test_必須日付と不正日付は422() -> None:
    with create_client() as client:
        assert client.get("/api/analysis/users/1/training-report").status_code == 422
        assert client.get("/api/analysis/users/1/training-report?from=x&to=2026-08-01").status_code == 422
        assert client.get("/api/analysis/users/1/training-report?from=2026-08-02&to=2026-08-01").status_code == 422


def test_SpringのエラーをHTTPへ変換する() -> None:
    class ErrorClient:
        async def get_analysis_data(self, *args: object) -> AnalysisData:
            raise SpringApiError(404, "指定されたユーザーが見つかりません。")
    with create_client(ErrorClient()) as client:
        response = client.get("/api/analysis/users/1/training-report?from=2026-08-01&to=2026-08-31")
    assert response.status_code == 404


def test_Spring接続失敗とタイムアウトを変換する() -> None:
    class ErrorClient:
        def __init__(self, status_code: int) -> None:
            self.status_code = status_code

        async def get_analysis_data(self, *args: object) -> AnalysisData:
            message = "Spring Boot API に接続できませんでした。" if self.status_code == 503 \
                else "分析データの取得がタイムアウトしました。"
            raise SpringApiError(self.status_code, message)

    for status_code in (503, 504):
        with create_client(ErrorClient(status_code)) as client:
            response = client.get("/api/analysis/users/1/training-report?from=2026-08-01&to=2026-08-31")
        assert response.status_code == status_code
