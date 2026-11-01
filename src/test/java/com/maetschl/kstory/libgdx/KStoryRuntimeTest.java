package com.maetschl.kstory.libgdx;

import com.badlogic.gdx.utils.JsonReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KStoryRuntimeTest {
    @Test
    void configNormalizesPortableAssetRoot() {
        KStoryConfig config = KStoryConfig.builder().assetRoot("content").sourceLocale("en").build();
        assertEquals("content/", config.assetRoot());
        assertEquals("en", config.sourceLocale());
    }

    @Test
    void actionPreservesTypedParametersAndCallback() {
        KStoryAction action = KStoryAction.fromJson(
                "action",
                new JsonReader().parse("{callback_id:\"repair\",item_id:\"HAMMER\",amount:2}")
        );
        assertEquals("action", action.type());
        assertEquals("repair", action.callbackId());
        assertEquals("HAMMER", action.parameter("item_id"));
        assertEquals("2", action.parameter("amount"));
    }

    @Test
    void defaultAdapterDoesNotPretendGameplayIsSupported() {
        KStoryGameAdapter adapter = new KStoryGameAdapter() {};
        KStoryAction action = KStoryAction.fromJson("camera_shake_start", null);
        assertFalse(adapter.execute(action).supported);
        assertTrue(adapter.evaluate(KStoryCondition.fromJson(null)));
    }
}
