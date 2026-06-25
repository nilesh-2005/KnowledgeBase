export class ChatApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'ChatApiError';
    this.status = status;
  }
}

export function getChatErrorMessage(status: number, serverMessage?: string): string {
  switch (status) {
    case 401:
      return 'Your session has expired. Please sign in again.';
    case 403:
      return 'You do not have permission to use the AI Assistant.';
    case 404:
      return 'The requested resource could not be found.';
    case 500:
      return 'An unexpected server error occurred. Please try again later.';
    case 503:
      return serverMessage || 'The AI service is temporarily unavailable. Please try again later.';
    case 504:
      return serverMessage || 'The request timed out while generating an answer. Please try again.';
    default:
      return serverMessage || 'Something went wrong. Please try again.';
  }
}
