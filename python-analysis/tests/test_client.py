import httpx
import pytest
import respx
from app.clients.spring_api import SpringApiClient, SpringApiError

DATA = {"userId": 1, "sessions": [], "records": [], "matches": []}


@respx.mock
@pytest.mark.asyncio
async def test_正常な分析データを取得する() -> None:
    respx.get("http://spring/api/analysis-data/users/1").mock(return_value=httpx.Response(200, json=DATA))
    client = SpringApiClient("http://spring", 1)
    try:
        assert (await client.get_analysis_data(1, "2026-08-01", "2026-08-31", "req")).user_id == 1
    finally:
        await client.close()


@pytest.mark.parametrize(("exception", "status"), [
    (httpx.ConnectError("接続失敗"), 503), (httpx.ReadTimeout("時間切れ"), 504),
])
@respx.mock
@pytest.mark.asyncio
async def test_通信例外を変換する(exception: Exception, status: int) -> None:
    respx.get("http://spring/api/analysis-data/users/1").mock(side_effect=exception)
    client = SpringApiClient("http://spring", 1)
    try:
        with pytest.raises(SpringApiError) as captured:
            await client.get_analysis_data(1, "2026-08-01", "2026-08-31", "req")
        assert captured.value.status_code == status
    finally:
        await client.close()


@pytest.mark.parametrize(("response", "status"), [(httpx.Response(401), 401),
                                                (httpx.Response(403), 403),
                                                (httpx.Response(404), 404),
                                                (httpx.Response(200, content=b"not-json"), 502)])
@respx.mock
@pytest.mark.asyncio
async def test_不正な応答を変換する(response: httpx.Response, status: int) -> None:
    respx.get("http://spring/api/analysis-data/users/1").mock(return_value=response)
    client = SpringApiClient("http://spring", 1)
    try:
        with pytest.raises(SpringApiError) as captured:
            await client.get_analysis_data(1, "2026-08-01", "2026-08-31", "req")
        assert captured.value.status_code == status
    finally:
        await client.close()


@respx.mock
@pytest.mark.asyncio
async def test_ユーザーごとのトークンをリクエスト単位で転送する() -> None:
    route = respx.get("http://spring/api/analysis-data/users/1").mock(return_value=httpx.Response(200, json=DATA))
    client = SpringApiClient("http://spring", 1)
    try:
        await client.get_analysis_data(1, "2026-08-01", "2026-08-31", "req-1", "Bearer token-1")
        await client.get_analysis_data(1, "2026-08-01", "2026-08-31", "req-2", "Bearer token-2")
        assert route.calls[0].request.headers["Authorization"] == "Bearer token-1"
        assert route.calls[1].request.headers["Authorization"] == "Bearer token-2"
        assert "Authorization" not in client._client.headers
    finally:
        await client.close()
