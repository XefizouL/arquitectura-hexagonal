// by from ia
package com.tienda.api.domain.port.out;

import java.util.function.Supplier;

public interface TransaccionPort {

    <T> T ejecutar(Supplier<T> operacion);
}
