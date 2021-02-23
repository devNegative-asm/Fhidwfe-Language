package interfaceCore;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IODevice extends Supplier<Byte>, Consumer<Byte> {

}
