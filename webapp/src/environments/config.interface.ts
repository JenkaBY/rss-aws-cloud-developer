export type ApiEndpoint = 'product' | 'order' | 'import' | 'bff' | 'cart' | 'auth';

export interface Config {
  production: boolean;
  apiEndpoints: Record<ApiEndpoint, string>;
  apiEndpointsEnabled: Record<ApiEndpoint, boolean>;
  accessibleUrl: string;
  taskIdentification: string;
}
