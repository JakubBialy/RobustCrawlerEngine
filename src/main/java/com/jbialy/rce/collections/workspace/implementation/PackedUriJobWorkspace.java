package com.jbialy.rce.collections.workspace.implementation;

import com.jbialy.rce.collections.PackedCollection;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

public class PackedUriJobWorkspace extends GeneralPurposeJobWorkspace<URI> {

    private PackedUriJobWorkspace(Set<URI> allItems, Queue<URI> todo, Set<URI> inProgress, Set<URI> done, Set<URI> damaged) {
        super(allItems, todo, inProgress, done, damaged);
    }

    public static PackedUriJobWorkspace createWithCommonLeftAndRightPart(String leftPart, String rightPart) {
        final Function<URI, byte[]> packer = uri -> uri.toString().replaceFirst(leftPart, "").replaceAll(rightPart + "$", "").getBytes(StandardCharsets.UTF_8);
        final Function<byte[], URI> unpacker = str -> URI.create(leftPart + new String(str, StandardCharsets.UTF_8) + rightPart);

        return createPackedCollectionJobWorkspace(packer, unpacker);
    }

    public static PackedUriJobWorkspace createWithCommonRightPart(String rightPart) {
        final Function<URI, byte[]> packer = uri -> uri.toString().replaceAll(rightPart + "$", "").getBytes(StandardCharsets.UTF_8);
        final Function<byte[], URI> unpacker = str -> URI.create(new String(str, StandardCharsets.UTF_8) + rightPart);

        return createPackedCollectionJobWorkspace(packer, unpacker);
    }

    public static PackedUriJobWorkspace createWithCommonLeftPart(String leftPart) {
        final Function<URI, byte[]> packer = uri -> uri.toString().replace(leftPart, "").getBytes(StandardCharsets.UTF_8);
        final Function<byte[], URI> unpacker = str -> URI.create(leftPart + new String(str, StandardCharsets.UTF_8));

        return createPackedCollectionJobWorkspace(packer, unpacker);
    }

    @NotNull
    private static <T> PackedUriJobWorkspace createPackedCollectionJobWorkspace(Function<URI, T> packer, Function<T, URI> unpacker) {
        return new PackedUriJobWorkspace(
                new PackedCollection<>(packer, unpacker),
                new PackedCollection<>(packer, unpacker),
                new PackedCollection<>(packer, unpacker),
                new PackedCollection<>(packer, unpacker),
                new PackedCollection<>(packer, unpacker)
        );
    }

}
