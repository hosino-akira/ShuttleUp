from functools import lru_cache
from pydantic import AnyHttpUrl
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """環境変数から読み込むアプリケーション設定。"""

    spring_api_base_url: AnyHttpUrl
    spring_api_timeout_seconds: float = 10.0
    cors_allowed_origins: list[str] = ["http://localhost:5173"]
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


@lru_cache
def get_settings() -> Settings:
    return Settings()
