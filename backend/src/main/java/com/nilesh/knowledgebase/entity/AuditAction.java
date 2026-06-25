package com.nilesh.knowledgebase.entity;

/**
 * Audit event types tracked in the audit_logs table.
 * Kept intentionally small — extend as new features are added.
 */
public enum AuditAction {
    DOCUMENT_UPLOAD,
    DOCUMENT_DELETE,
    USER_ROLE_CHANGE,
    USER_DELETE,
    COLLECTION_CREATE,
    COLLECTION_DELETE,
    DOCUMENT_REPROCESSED
}
