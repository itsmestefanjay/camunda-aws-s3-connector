package de.novatec.bpm.camunda.connector.file.api.impl;

import de.novatec.bpm.camunda.connector.file.api.RemoteFileCommand;
import de.novatec.bpm.camunda.connector.file.api.ProcessFileCommand;
import de.novatec.bpm.camunda.connector.file.api.LocalFileCommand;
import de.novatec.bpm.camunda.connector.file.api.impl.model.FileContent;
import de.novatec.bpm.camunda.connector.file.api.impl.model.RequestData;

import java.io.IOException;
import java.util.Objects;

public class ProcessFileService implements ProcessFileCommand {

    private final RemoteFileCommand remoteFileCommand;
    private final LocalFileCommand localFileCommand;

    public ProcessFileService(RemoteFileCommand remoteFileCommand, LocalFileCommand localFileCommand) {
        this.remoteFileCommand = remoteFileCommand;
        this.localFileCommand = localFileCommand;
    }

    public RequestData uploadFile(RequestData request) throws IOException {
        String contentType = Objects.requireNonNull(request.getContentType(), "Content type must be set");
        byte[] content = localFileCommand.loadFile(request.getFilePath());
        remoteFileCommand.putObject(request, FileContent.builder()
                .content(content)
                .contentLength((long) content.length)
                .contentType(contentType)
                .build()
        );
        return request;
    }

    public RequestData deleteFile(RequestData request) throws IOException {
        localFileCommand.deleteFile(request.getFilePath());
        remoteFileCommand.deleteObject(request);
        return request;
    }

    public RequestData downloadFile(RequestData request) throws IOException {
        FileContent response = remoteFileCommand.getObject(request);
        localFileCommand.saveFile(response.getContent(), request.getFilePath());
        // overwrite with actual content type
        request.setContentType(request.getContentType());
        return request;
    }
}
