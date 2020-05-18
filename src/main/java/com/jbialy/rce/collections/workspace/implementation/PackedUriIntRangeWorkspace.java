package com.jbialy.rce.collections.workspace.implementation;

import com.jbialy.rce.collections.RangeIntUriSet;

import java.net.URI;

public class PackedUriIntRangeWorkspace extends GeneralPurposeWorkspace<URI> {

    public PackedUriIntRangeWorkspace(String leftPart, String rightPart) {
        super(
                new RangeIntUriSet(leftPart, rightPart),
                new RangeIntUriSet(leftPart, rightPart),
                new RangeIntUriSet(leftPart, rightPart),
                new RangeIntUriSet(leftPart, rightPart),
                new RangeIntUriSet(leftPart, rightPart)
        );
    }
}
