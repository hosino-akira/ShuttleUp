import logging
import time
import uuid
from datetime import date
from fastapi import APIRouter, HTTPException, Query, Request
from app.clients.spring_api import SpringApiClient, SpringApiError
from app.models.report import TrainingReport
from app.services.report_service import create_training_report

router = APIRouter(prefix="/api/analysis", tags=["トレーニング分析"])
logger = logging.getLogger(__name__)


@router.get("/users/{user_id}/training-report", response_model=TrainingReport)
async def get_training_report(request: Request, user_id: int,
                              from_date: date = Query(alias="from"),
                              to_date: date = Query(alias="to")) -> TrainingReport:
    """指定期間のトレーニング分析結果を返す。"""
    if from_date > to_date:
        raise HTTPException(status_code=422, detail="開始日は終了日以前の日付を指定してください。")
    request_id = request.headers.get("X-Request-ID") or str(uuid.uuid4())
    started = time.perf_counter()
    client: SpringApiClient = request.app.state.spring_api_client
    try:
        data = await client.get_analysis_data(user_id, from_date.isoformat(), to_date.isoformat(), request_id)
        report = create_training_report(data, from_date, to_date)
        logger.info("analysis_completed request_id=%s user_id=%s from=%s to=%s elapsed_ms=%.1f sessions=%s records=%s matches=%s",
                    request_id, user_id, from_date, to_date, (time.perf_counter() - started) * 1000,
                    len(data.sessions), len(data.records), len(data.matches))
        return report
    except SpringApiError as exc:
        logger.warning("analysis_failed request_id=%s user_id=%s error_type=%s",
                       request_id, user_id, type(exc).__name__)
        raise HTTPException(status_code=exc.status_code, detail=exc.message) from exc
