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

public class CloudinaryHandler {
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
                return new Response(ResponseStatus.SUCCESS, "Create Cloudinary upload signature successfully", responseDTO);
            } catch (IllegalArgumentException e) {
                return new Response(ResponseStatus.INVALID_INPUT, e.getMessage());
            } catch (IllegalStateException e) {
                return new Response(ResponseStatus.ERROR, e.getMessage());
            }
        }
    }
}
