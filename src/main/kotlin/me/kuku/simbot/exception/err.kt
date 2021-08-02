package me.kuku.simbot.exception

class WaitNextMessageTimeoutException: RuntimeException("")

class VerifyFailedException(msg: String): RuntimeException(msg)