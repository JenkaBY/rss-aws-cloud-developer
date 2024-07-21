import { AppRequest } from '../models';

/**
 * @param {AppRequest} request
 * @returns {string}
 */
export function getUserIdFromRequest(request: AppRequest): string {
  return request.user && request.user.id || "6c9627b6-dc2a-4300-95de-2792c8d36b5e";
}
