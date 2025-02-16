package ch.framedev.simplejsonutils;



/*
 * ch.framedev.simplejsonutils
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 16.02.2025 14:35
 */

/**
 * Flags for configuring JSON parsing behavior.
 */
public enum Flag {

    /**
     * Enables pretty-printing of JSON output.
     */
    PRETTY_PRINT,

    /**
     * Enables indentation (useful when separate from PRETTY_PRINT).
     */
    USE_INDENT,

    /**
     * Enables debugging logs during serialization/deserialization.
     */
    DEBUG;
}