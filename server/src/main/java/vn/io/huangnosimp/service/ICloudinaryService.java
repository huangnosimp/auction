package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.CloudinaryUploadSignatureResponseDTO;

public interface ICloudinaryService {
    CloudinaryUploadSignatureResponseDTO createUploadSignature(String userId, String requestedFolder);
}
