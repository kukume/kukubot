package me.kuku.yuq.exception


class VerifyFailedException(msg: String): RuntimeException(msg)

class BaiduAiException(msg: String): RuntimeException(msg)

class BaiduException(msg: String): RuntimeException(msg)