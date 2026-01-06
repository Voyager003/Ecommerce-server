package com.ecommerce.infra.pg;

public interface PgClient {

    PgResponse approve(PgRequest request);

    PgResponse cancel(String transactionId, long amount);

    PgResponse inquiry(String transactionId);
}
