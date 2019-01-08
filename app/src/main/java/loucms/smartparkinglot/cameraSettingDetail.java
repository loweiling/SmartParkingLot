package loucms.smartparkinglot;

import android.hardware.camera2.CaptureRequest;

/**
 * Created by loucms on 11/27/17.
 */

public class cameraSettingDetail {
    private String COLOR_CORRECTION_ABERRATION_MODE;
    private String CONTROL_AE_ANTIBANDING_MODE;
    private String CONTROL_AE_LOCK;
    private String CONTROL_AE_MODE;
    private String CONTROL_AF_MODE;
    private String CONTROL_AWB_LOCK;
    private String CONTROL_AWB_MODE;
    private String BLACK_LEVEL_LOCK;
    private String CONTROL_EFFECT_MODE;
    private String CONTROL_MODE;
    private String CONTROL_SCENE_MODE;
    private String CONTROL_VIDEO_STABILIZATION_MODE;
    private String NOISE_REDUCTION_MODE;
    private String SENSOR_TEST_PATTERN_MODE;
    public void setCOLOR_CORRECTION_ABERRATION_MODE(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            COLOR_CORRECTION_ABERRATION_MODE = "FAST";
            builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_FAST);


        }else if(state.equals("FAST")){
            COLOR_CORRECTION_ABERRATION_MODE ="HQ";
            builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);

        }else if(state.equals("HQ")){
            COLOR_CORRECTION_ABERRATION_MODE ="OFF";
            builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF);

        }
    }
    public String getCOLOR_CORRECTION_ABERRATION_MODE(){
        return COLOR_CORRECTION_ABERRATION_MODE;
    }
    public void setCONTROL_AE_ANTIBANDING_MODE(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            CONTROL_AE_ANTIBANDING_MODE= "50HZ";
            builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                    CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_50HZ);

        }else if(state.equals("50HZ")){
            CONTROL_AE_ANTIBANDING_MODE="60HZ";
            builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                    CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_60HZ);

        }else if(state.equals("60HZ")){
            CONTROL_AE_ANTIBANDING_MODE="AUTO";
            builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                    CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);

        }
        else if(state.equals("AUTO")){
            CONTROL_AE_ANTIBANDING_MODE="OFF";
            builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                    CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_OFF);
        }
    }
    public String getCONTROL_AE_ANTIBANDING_MODE(){
        return CONTROL_AE_ANTIBANDING_MODE;
    }
    public void setCONTROL_AE_LOCK(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            CONTROL_AE_LOCK = "ON";
            builder.set(CaptureRequest.CONTROL_AE_LOCK,
                    true);
        }else if(state.equals("ON")){
            CONTROL_AE_LOCK = "OFF";
            builder.set(CaptureRequest.CONTROL_AE_LOCK,
                    false);
        }
    }
    public String getCONTROL_AE_LOCK(){
        return CONTROL_AE_LOCK;
    }
    public void setCONTROL_AE_MODE(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            CONTROL_AE_MODE ="ON";
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON);
        }else if(state.equals("ON")){
            CONTROL_AE_MODE ="ON_AUTO_FLASH";
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }else if(state.equals("ON_AUTO_FLASH")){
            CONTROL_AE_MODE ="ON_ALWAYS_FLASH";
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
        }else if(state.equals("ON_ALWAYS_FLASH")){
            CONTROL_AE_MODE ="ON_AUTO_FLASH_REDEYE";
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE);
        }
        else if(state.equals("ON_AUTO_FLASH_REDEYE")){
            CONTROL_AE_MODE ="OFF";
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_OFF);
        }
    }
    public String getCONTROL_AE_MODE(){
        return CONTROL_AE_MODE;
    }
    public void setCONTROL_AF_MODE(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            CONTROL_AF_MODE="AUTO";
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_AUTO);
        }else if(state.equals("AUTO")){
            CONTROL_AF_MODE="MACRO";
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_MACRO);
        }else if(state.equals("MACRO")){
            CONTROL_AF_MODE="ONTINUOUS_VIDEO";
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
        }else if(state.equals("ONTINUOUS_VIDEO")){
            CONTROL_AF_MODE="CONTINUOUS_PICTURE";
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        }else if(state.equals("CONTINUOUS_PICTURE")){
            CONTROL_AF_MODE="EDOF";
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_EDOF);
        }else if(state.equals("EDOF")){
            CONTROL_AF_MODE="OFF";
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_OFF);
        }
    }
    public String getCONTROL_AF_MODE(){
        return CONTROL_AF_MODE;
    }
    public void setCONTROL_AWB_LOCK(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            CONTROL_AWB_LOCK="ON";
            builder.set(CaptureRequest.CONTROL_AWB_LOCK,
                    true);
        }else if(state.equals("ON")){
            CONTROL_AWB_LOCK="OFF";
            builder.set(CaptureRequest.CONTROL_AWB_LOCK,
                    false);
        }
    }
    public String getCONTROL_AWB_LOCK(){
        return CONTROL_AWB_LOCK;
    }
    public void setCONTROL_AWB_MODE(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            CONTROL_AWB_MODE="AUTO";
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_AUTO);
        }else if(state.equals("AUTO")){
            CONTROL_AWB_MODE="INCANDESCENT";
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT);
        }else if(state.equals("INCANDESCENT")){
            CONTROL_AWB_MODE="FLUORESCENT";
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT);
        }else if(state.equals("FLUORESCENT")){
            CONTROL_AWB_MODE="WARM_FLUORESCENT";
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_WARM_FLUORESCENT);
        }else if(state.equals("WARM_FLUORESCENT")){
            CONTROL_AWB_MODE="DAYLIGHT";
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT);
        }else if(state.equals("DAYLIGHT")){
            CONTROL_AWB_MODE="CLOUDY_DAYLIGHT";
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);
        }
        else if(state.equals("CLOUDY_DAYLIGHT")){
            CONTROL_AWB_MODE="TWILIGHT";
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_TWILIGHT);
        }else if(state.equals("TWILIGHT")){
            CONTROL_AWB_MODE="SHADE";
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_SHADE);
        }else if(state.equals("SHADE")){
            CONTROL_AWB_MODE="OFF";
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_OFF);
        }
    }
    public String getCONTROL_AWB_MODE(){
        return CONTROL_AWB_MODE;
    }
    public void setBLACK_LEVEL_LOCK(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            BLACK_LEVEL_LOCK= "ON";
            builder.set(CaptureRequest.BLACK_LEVEL_LOCK,
                    true);
        }else if(state.equals("ON")){
            BLACK_LEVEL_LOCK="OFF";
            builder.set(CaptureRequest.BLACK_LEVEL_LOCK,
                    false);
        }
    }
    public String getBLACK_LEVEL_LOCK(){
        return BLACK_LEVEL_LOCK;
    }
    public void setCONTROL_EFFECT_MODE(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            CONTROL_EFFECT_MODE = "MONO";
            builder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                    CaptureRequest.CONTROL_EFFECT_MODE_MONO);

        }else if(state.equals("MONO")){
            CONTROL_EFFECT_MODE = "NEGATIVE";
            builder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                    CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE);

        }else if(state.equals("NEGATIVE")){
            CONTROL_EFFECT_MODE = "SOLARIZE";
            builder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                    CaptureRequest.CONTROL_EFFECT_MODE_SOLARIZE);

        }else if(state.equals("SOLARIZE")){
            CONTROL_EFFECT_MODE = "SEPIA";
            builder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                    CaptureRequest.CONTROL_EFFECT_MODE_SEPIA);

        }else if(state.equals("SEPIA")){
            CONTROL_EFFECT_MODE = "WHITEBOARD";
            builder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                    CaptureRequest.CONTROL_EFFECT_MODE_WHITEBOARD);

        }else if(state.equals("WHITEBOARD")){
            CONTROL_EFFECT_MODE = "BLACKBOARD";
            builder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                    CaptureRequest.CONTROL_EFFECT_MODE_BLACKBOARD);

        }
        else if(state.equals("BLACKBOARD")){
            CONTROL_EFFECT_MODE = "AQUA";
            builder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                    CaptureRequest.CONTROL_EFFECT_MODE_AQUA);

        }else if(state.equals("AQUA")){
            CONTROL_EFFECT_MODE = "OFF";
            builder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                    CaptureRequest.CONTROL_EFFECT_MODE_OFF);

        }
    }
    public String getCONTROL_EFFECT_MODE(){
        return CONTROL_EFFECT_MODE;
    }
    public void setCONTROL_MODE(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            CONTROL_MODE="AUTO";
            builder.set(CaptureRequest.CONTROL_MODE,
                    CaptureRequest.CONTROL_MODE_AUTO);
        }else if(state.equals("AUTO")){
            CONTROL_MODE="USE_SCENE_MODE";
            builder.set(CaptureRequest.CONTROL_MODE,
                    CaptureRequest.CONTROL_MODE_USE_SCENE_MODE);
        }else if(state.equals("USE_SCENE_MODE")){
            CONTROL_MODE="OFF_KEEP_STATE";
            builder.set(CaptureRequest.CONTROL_MODE,
                    CaptureRequest.CONTROL_MODE_OFF_KEEP_STATE);
        }else if(state.equals("OFF_KEEP_STATE")){
            CONTROL_MODE="OFF";
            builder.set(CaptureRequest.CONTROL_MODE,
                    CaptureRequest.CONTROL_MODE_OFF);
        }
    }
    public String getCONTROL_MODE(){
        return CONTROL_MODE;
    }
    public void setCONTROL_SCENE_MODE(CaptureRequest.Builder builder,String state){
        if(state.equals("DISABLED")){
            CONTROL_SCENE_MODE="FACE_PRIORITY";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY);

        }else if(state.equals("FACE_PRIORITY")){
            CONTROL_SCENE_MODE="ACTION";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY);

        }else if(state.equals("ACTION")){
            CONTROL_SCENE_MODE="PORTRAIT";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_PORTRAIT);

        }else if(state.equals("PORTRAIT")){
            CONTROL_SCENE_MODE="LANDSCAPE";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_LANDSCAPE);

        }else if(state.equals("LANDSCAPE")){
            CONTROL_SCENE_MODE="NIGHT";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_NIGHT);

        }else if(state.equals("NIGHT")){
            CONTROL_SCENE_MODE="NIGHT_PORTRAIT";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_NIGHT_PORTRAIT);

        }
        else if(state.equals("NIGHT_PORTRAIT")){
            CONTROL_SCENE_MODE="THEATRE";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_THEATRE);

        }else if(state.equals("THEATRE")){
            CONTROL_SCENE_MODE="BEACH";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_BEACH);

        }else if(state.equals("BEACH")){
            CONTROL_SCENE_MODE="SNOW";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_SNOW);

        }else if(state.equals("SNOW")){
            CONTROL_SCENE_MODE="SUNSET";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_SUNSET);

        }else if(state.equals("SUNSET")){
            CONTROL_SCENE_MODE="STEADYPHOTO";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_STEADYPHOTO);

        }else if(state.equals("STEADYPHOTO")){
            CONTROL_SCENE_MODE="FIREWORKS";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_FIREWORKS);

        }else if(state.equals("FIREWORKS")){
            CONTROL_SCENE_MODE="SPORTS";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_SPORTS);

        }else if(state.equals("SPORTS")){
            CONTROL_SCENE_MODE="PARTY";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_PARTY);

        }else if(state.equals("PARTY")){
            CONTROL_SCENE_MODE="CANDLELIGHT";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_CANDLELIGHT);

        }else if(state.equals("CANDLELIGHT")){
            CONTROL_SCENE_MODE="BARCODE";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_BARCODE);

        }else if(state.equals("BARCODE")){
            CONTROL_SCENE_MODE="HIGH_SPEED_VIDEO";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_HIGH_SPEED_VIDEO);

        }else if(state.equals("HIGH_SPEED_VIDEO")){
            CONTROL_SCENE_MODE="HDR";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_HDR);

        }
        else if(state.equals("HDR")){
            CONTROL_SCENE_MODE="DISABLED";
            builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                    CaptureRequest.CONTROL_SCENE_MODE_DISABLED);

        }
    }
    public String getCONTROL_SCENE_MODE(){
        return CONTROL_SCENE_MODE;
    }
    public void setCONTROL_VIDEO_STABILIZATION_MODE(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            CONTROL_VIDEO_STABILIZATION_MODE = "ON";
            builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
        }else if(state.equals("ON")){
            CONTROL_VIDEO_STABILIZATION_MODE = "OFF";
            builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);
        }
    }
    public String getCONTROL_VIDEO_STABILIZATION_MODE(){
        return CONTROL_VIDEO_STABILIZATION_MODE;
    }
    public void setNOISE_REDUCTION_MODE(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            NOISE_REDUCTION_MODE = "FAST";
            builder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                    CaptureRequest.NOISE_REDUCTION_MODE_FAST);
        }else if(state.equals("FAST")){
            NOISE_REDUCTION_MODE = "HIGH_QUALITY";
            builder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                    CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY);
        }else if(state.equals("HIGH_QUALITY")){
            NOISE_REDUCTION_MODE = "MINIMAL";
            builder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                    CaptureRequest.NOISE_REDUCTION_MODE_MINIMAL);
        }
        else if(state.equals("MINIMAL")){
            NOISE_REDUCTION_MODE = "ZERO_SHUTTER_LAG";
            builder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                    CaptureRequest.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG);
        }else if(state.equals("ZERO_SHUTTER_LAG")){
            NOISE_REDUCTION_MODE = "OFF";
            builder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                    CaptureRequest.NOISE_REDUCTION_MODE_OFF);
        }
    }
    public String getNOISE_REDUCTION_MODE(){
        return NOISE_REDUCTION_MODE;
    }
    public void setSENSOR_TEST_PATTERN_MODE(CaptureRequest.Builder builder,String state){
        if(state.equals("OFF")){
            SENSOR_TEST_PATTERN_MODE = "SOLID_COLOR";
            builder.set(CaptureRequest.SENSOR_TEST_PATTERN_MODE,
                    CaptureRequest.SENSOR_TEST_PATTERN_MODE_SOLID_COLOR);
        }else if(state.equals("SOLID_COLOR")){
            SENSOR_TEST_PATTERN_MODE = "COLOR_BARS";
            builder.set(CaptureRequest.SENSOR_TEST_PATTERN_MODE,
                    CaptureRequest.SENSOR_TEST_PATTERN_MODE_COLOR_BARS);
        }else if(state.equals("COLOR_BARS")){
            SENSOR_TEST_PATTERN_MODE = "COLOR_BARS_FADE_TO_GRAY";
            builder.set(CaptureRequest.SENSOR_TEST_PATTERN_MODE,
                    CaptureRequest.SENSOR_TEST_PATTERN_MODE_COLOR_BARS_FADE_TO_GRAY);
        }
        else if(state.equals("COLOR_BARS_FADE_TO_GRAY")){
            SENSOR_TEST_PATTERN_MODE = "PN9";
            builder.set(CaptureRequest.SENSOR_TEST_PATTERN_MODE,
                    CaptureRequest.SENSOR_TEST_PATTERN_MODE_PN9);
        }else if(state.equals("PN9")){
            SENSOR_TEST_PATTERN_MODE = "CUSTOM1";
            builder.set(CaptureRequest.SENSOR_TEST_PATTERN_MODE,
                    CaptureRequest.SENSOR_TEST_PATTERN_MODE_CUSTOM1);
        }else if(state.equals("CUSTOM1")){
            SENSOR_TEST_PATTERN_MODE = "OFF";
            builder.set(CaptureRequest.SENSOR_TEST_PATTERN_MODE,
                    CaptureRequest.SENSOR_TEST_PATTERN_MODE_OFF);
        }
    }
    public String getSENSOR_TEST_PATTERN_MODE(){
        return SENSOR_TEST_PATTERN_MODE;
    }


}
