def rate(numerator: int | float, denominator: int | float) -> float:
    """割合を小数第1位で返す。"""
    return 0.0 if denominator == 0 else round(float(numerator) / float(denominator) * 100, 1)
