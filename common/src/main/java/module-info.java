module vn.io.huangnosimp.common {
    requires transitive com.google.gson;
    
    exports vn.io.huangnosimp.enums;
    exports vn.io.huangnosimp.util;
    exports vn.io.huangnosimp.protocol;
    exports vn.io.huangnosimp.dto.shared;
    exports vn.io.huangnosimp.dto.request;
    exports vn.io.huangnosimp.dto.response;
    
    opens vn.io.huangnosimp.dto.shared to com.google.gson;
    opens vn.io.huangnosimp.dto.request to com.google.gson;
    opens vn.io.huangnosimp.dto.response to com.google.gson;
}
