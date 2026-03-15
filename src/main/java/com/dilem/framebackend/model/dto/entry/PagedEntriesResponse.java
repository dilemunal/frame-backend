package com.dilem.framebackend.model.dto.entry;

import java.util.List;

public record PagedEntriesResponse(
    List<EntryResponse> entries,
    int page,
    boolean hasMore
) {}
