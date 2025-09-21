package com.kjr.rfp.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileStorageService {
    @Autowired
    private GridFsOperations gridFsOperations;

    public String storeFile(MultipartFile file) throws IOException {
        return gridFsOperations.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType()
        ).toString();
    }

    public GridFsResource getFileResource(String fileId) {
        GridFSFile file = gridFsOperations.findOne(new Query(Criteria.where("_id").is(fileId)));
        return file != null ? gridFsOperations.getResource(file) : null;
    }
}

