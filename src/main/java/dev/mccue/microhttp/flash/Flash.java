package dev.mccue.microhttp.flash;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.microhttp.handler.Handler;
import dev.mccue.microhttp.session.ScopedSession;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class Flash {
    private Flash() {}

    private static final ScopedValue<Json> FLASH = ScopedValue.newInstance();
    private static final ScopedValue<AtomicReference<Json>> NEXT_FLASH_REF = ScopedValue.newInstance();

    public static Optional<Json> get() {
        return Optional.ofNullable(FLASH.get());
    }

    public static <T> Optional<T> get(JsonDecoder<? extends T> decoder) {
        return get().map(decoder::decode);
    }

    public static void flash(Json value) {
        NEXT_FLASH_REF.get().set(value);
    }

    public static Handler wrap(Handler handler) {
        return request -> {
            var flash = ScopedSession.get()
                    .get("_flash")
                    .orElse(null);

            var nextFlashRef = new AtomicReference<Json>(null);
            var response = ScopedValue
                    .where(FLASH, flash)
                    .where(NEXT_FLASH_REF, nextFlashRef)
                    .call(() -> handler.handle(request))
                    .intoResponse();
            ScopedSession.update(data -> {
                data = data.without("_flash");
                var nextFlash = nextFlashRef.get();
                if (nextFlash != null) {
                    data = data.with("_flash", nextFlash);
                }
                return data;
            });
            return () -> response;
        };
    }
}
