import logging
import time
import httpx
from pydantic import ValidationError
from app.models.spring import AnalysisData

logger = logging.getLogger(__name__)


class SpringApiError(Exception):
    """Spring Boot API呼び出しの基底例外。"""

    def __init__(self, status_code: int, message: str) -> None:
        super().__init__(message)
        self.status_code = status_code
        self.message = message


class SpringApiClient:
    """分析用データをSpring Bootから取得する非同期クライアント。"""

    def __init__(self, base_url: str, timeout_seconds: float) -> None:
        self._client = httpx.AsyncClient(base_url=base_url.rstrip("/"), timeout=timeout_seconds)

    async def close(self) -> None:
        await self._client.aclose()

    async def get_analysis_data(self, user_id: int, from_date: str, to_date: str,
                                request_id: str) -> AnalysisData:
        started = time.perf_counter()
        try:
            response = await self._client.get(
                f"/api/analysis-data/users/{user_id}",
                params={"from": from_date, "to": to_date},
            )
            if response.status_code == 404:
                raise SpringApiError(404, "指定されたユーザーが見つかりません。")
            if response.status_code >= 500:
                raise SpringApiError(503, "Spring Boot API でエラーが発生しました。")
            if response.status_code >= 400:
                raise SpringApiError(400, "分析データを取得できませんでした。")
            try:
                return AnalysisData.model_validate(response.json())
            except (ValueError, ValidationError) as exc:
                raise SpringApiError(502, "Spring Boot API のレスポンス形式が不正です。") from exc
        except httpx.TimeoutException as exc:
            raise SpringApiError(504, "分析データの取得がタイムアウトしました。") from exc
        except httpx.RequestError as exc:
            raise SpringApiError(503, "Spring Boot API に接続できませんでした。") from exc
        finally:
            logger.info("spring_api_completed request_id=%s user_id=%s elapsed_ms=%.1f",
                        request_id, user_id, (time.perf_counter() - started) * 1000)
