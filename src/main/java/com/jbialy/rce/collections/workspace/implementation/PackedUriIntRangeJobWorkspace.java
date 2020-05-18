package com.jbialy.rce.collections.workspace.implementation;

import com.jbialy.rce.collections.RangeIntUriSet;

import java.net.URI;

public class PackedUriIntRangeJobWorkspace extends GeneralPurposeJobWorkspace<URI> {

    public PackedUriIntRangeJobWorkspace(String leftPart, String rightPart) {
        super(
                new RangeIntUriSet(leftPart, rightPart),
                new RangeIntUriSet(leftPart, rightPart),
                new RangeIntUriSet(leftPart, rightPart),
                new RangeIntUriSet(leftPart, rightPart),
                new RangeIntUriSet(leftPart, rightPart)
        );
    }
}
