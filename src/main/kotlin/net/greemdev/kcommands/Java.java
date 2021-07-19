package net.greemdev.kcommands;

import kotlin.Unit;
import kotlin.jvm.functions.*;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Java {

    private static final Kotlin REF = new Kotlin();

    private Java() {}

    public static Kotlin interop() {
        return REF;
    }

    public static class Kotlin {

        public <P1> Function1<P1, Unit> consumer(Consumer<P1> consumer) {
            return (parameter) -> {
                consumer.accept(parameter);
                return Unit.INSTANCE;
            };
        }

        public <P1> Function1<P1, Unit> noop() {
            return (parameter) -> Unit.INSTANCE;
        }

        public <P1, P2> Function2<P1, P2, Unit> biConsumer(BiConsumer<P1, P2> consumer) {
            return (p1, p2) -> {
                consumer.accept(p1, p2);
                return Unit.INSTANCE;
            };
        }

        public Function1<KEmbedBuilder, Unit> kEmbed(Consumer<KEmbedBuilder> consumer) {
            return this.consumer(consumer);
        }

        public Function1<ReplyAction, Unit> wrapReply(Consumer<ReplyAction> consumer) {
            return this.consumer(consumer);
        }

    }



}
