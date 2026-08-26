import logging
from contextlib import asynccontextmanager
from collections.abc import AsyncIterator
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.reports import router
from app.clients.spring_api import SpringApiClient
from app.core.config import get_settings

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s %(message)s")


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    """HTTPクライアントをアプリケーション単位で管理する。"""
    settings = get_settings()
    app.state.spring_api_client = SpringApiClient(str(settings.spring_api_base_url), settings.spring_api_timeout_seconds)
    yield
    await app.state.spring_api_client.close()


app = FastAPI(title="ShuttleUp トレーニング分析API", version="0.1.0", lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=get_settings().cors_allowed_origins,
    allow_credentials=True,
    allow_methods=["GET"],
    allow_headers=["*"],
)
app.include_router(router)
