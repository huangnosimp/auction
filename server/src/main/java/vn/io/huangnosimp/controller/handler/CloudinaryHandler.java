package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.dto.request.CloudinaryUploadSignatureRequestDTO;
import vn.io.huangnosimp.dto.response.CloudinaryUploadSignatureResponseDTO;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.service.ICloudinaryService;
import vn.io.huangnosimp.util.GsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudinaryHandler {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryHandler.class);

    public static class GetUploadSignatureHandler implements RequestHandler {
        private final ICloudinaryService cloudinaryService;

        public GetUploadSignatureHandler(ICloudinaryService cloudinaryService) {
            this.cloudinaryService = cloudinaryService;
        }

        @Override
        public Response handle(Request request, ClientHandle client) {
            try {
                CloudinaryUploadSignatureRequestDTO dto = GsonParser.GSON.fromJson(
                        GsonParser.GSON.toJsonTree(request.getData()),
                        CloudinaryUploadSignatureRequestDTO.class
                );
                String requestedFolder = dto == null ? null : dto.getFolder();
                CloudinaryUploadSignatureResponseDTO responseDTO = cloudinaryService.createUploadSignature(
                        client.getUserId(),
                        requestedFolder
                );
                logger.info("Created Cloudinary upload signature userId={} folder={}", client.getUserId(), requestedFolder);
                return new Response(ResponseStatus.SUCCESS, "Create Cloudinary upload signature successfully", responseDTO);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid Cloudinary upload signature request userId={}", client.getUserId(), e);
                return new Response(ResponseStatus.INVALID_INPUT, e.getMessage());
            } catch (IllegalStateException e) {
                logger.error("Could not create Cloudinary upload signature userId={}", client.getUserId(), e);
                return new Response(ResponseStatus.ERROR, e.getMessage());
            }
        }
    }
}
