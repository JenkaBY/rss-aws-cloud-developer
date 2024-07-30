export interface BackendResponse<T> {
  data: T,
  status: number,
  message: string
}
