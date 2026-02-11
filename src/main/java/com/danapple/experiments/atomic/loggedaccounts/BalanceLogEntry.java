package com.danapple.experiments.atomic.loggedaccounts;

import java.math.BigDecimal;

record BalanceLogEntry(BigDecimal delta,
                       BalanceLogState state,
                       long createTime) {}
