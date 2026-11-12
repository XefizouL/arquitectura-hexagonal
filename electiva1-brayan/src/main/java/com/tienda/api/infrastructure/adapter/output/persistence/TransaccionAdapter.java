// by from ia
package com.tienda.api.infrastructure.adapter.output.persistence;

import com.tienda.api.domain.port.out.TransaccionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class TransaccionAdapter implements TransaccionPort {

    private final TransactionTemplate template;

    public TransaccionAdapter(PlatformTransactionManager transactionManager) {
        this.template = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T ejecutar(Supplier<T> operacion) {
        return template.execute(status -> operacion.get());
    }
}
