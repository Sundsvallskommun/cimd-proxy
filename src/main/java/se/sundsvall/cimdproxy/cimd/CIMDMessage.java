package se.sundsvall.cimdproxy.cimd;

public record CIMDMessage(
    String destinationAddress,
    String content) { }

