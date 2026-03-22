-- ===============================
-- Task Tracker Database Schema
-- ===============================

CREATE TABLE tasks (
id BIGSERIAL PRIMARY KEY,

```
title VARCHAR(100) NOT NULL,
description VARCHAR(255),

status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

CONSTRAINT chk_status CHECK (status IN ('PENDING', 'COMPLETED'))
```

);

-- ===============================
-- Indexes
-- ===============================

CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
