package com.facebook.presto.jdbc.utils;

import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;

class InputStreamRedirecter
        implements Runnable
{
    private final InputStream sourceStream;
    private final OutputStream targetStream;

    public static ListenableFuture redirect(InputStream sourceStream, OutputStream targetStream, ExecutorService executor)
    {
        return listeningDecorator(executor).submit(new InputStreamRedirecter(sourceStream, targetStream));
    }

    private InputStreamRedirecter(InputStream sourceStream, OutputStream targetStream)
    {
        this.sourceStream = sourceStream;
        this.targetStream = targetStream;
    }

    @Override
    public void run()
    {
        try {
            ByteStreams.copy(sourceStream, targetStream);
        }
        catch (IOException e) {
            throw new RuntimeException("exception while copying stream", e);
        }
    }
}
