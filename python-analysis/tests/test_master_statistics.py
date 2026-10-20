import unittest

from app.analysis.balance import analyze_balance
from app.models.spring import TrainingRecord


class MasterStatisticsTest(unittest.TestCase):
    def record(self, **values):
        return TrainingRecord(id=1, training_session_id=1, **values)

    def test_rename_does_not_change_explicit_statistics_type(self):
        records = [self.record(category_name="技術に見える個人名", statistics_type="PHYSICAL", duration_minutes=12)]
        warnings = []
        result = analyze_balance(records, warnings)
        self.assertEqual(result.physical_record_count, 1)
        self.assertEqual(result.skill_record_count, 0)
        self.assertEqual(result.physical_duration_minutes, 12)
        self.assertEqual(warnings, [])

    def test_explicit_other_is_not_reported_as_unclassified(self):
        result = analyze_balance([self.record(category_name="自由", statistics_type="OTHER")], [])
        self.assertEqual(result.other_record_count, 1)
        self.assertEqual(result.unclassified_categories, [])

    def test_legacy_responses_still_use_keyword_classification(self):
        result = analyze_balance([self.record(category_name="筋力")], [])
        self.assertEqual(result.physical_record_count, 1)

    def test_camel_case_api_field_is_parsed(self):
        record = TrainingRecord.model_validate({"id": 1, "trainingSessionId": 1,
            "categoryName": "renamed", "statisticsType": "SKILL"})
        self.assertEqual(analyze_balance([record], []).skill_record_count, 1)


if __name__ == "__main__":
    unittest.main()
