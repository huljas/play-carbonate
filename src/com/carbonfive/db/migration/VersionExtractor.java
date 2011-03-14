package com.carbonfive.db.migration;

public interface VersionExtractor
{
    String extractVersion(String name);
}
