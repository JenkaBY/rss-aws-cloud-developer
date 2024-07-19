import {Injectable, Injector} from '@angular/core';
import {EMPTY, Observable} from 'rxjs';
import {ApiService} from '../../core/api.service';
import {switchMap} from 'rxjs/operators';
import {PreSignedUrl} from "../../core/signed-url.interface";

@Injectable()
export class ManageProductsService extends ApiService {
  constructor(injector: Injector) {
    super(injector);
  }

  uploadProductsCSV(file: File): Observable<unknown> {
    if (!this.endpointEnabled('import')) {
      console.warn(
        'Endpoint "import" is disabled. To enable change your environment.ts config'
      );
      return EMPTY;
    }

    return this.getPreSignedUrl(file.name).pipe(
      switchMap((preSignedUrl: PreSignedUrl) => {
        console.log("presigned URL", preSignedUrl.url);
        return this.http.put(preSignedUrl.url, file, {
          headers: {
            // eslint-disable-next-line @typescript-eslint/naming-convention
            'Content-Type': 'text/csv',
          },
        })
      }
      )
    );
  }

  private getPreSignedUrl(fileName: string): Observable<PreSignedUrl> {
    const url = this.getUrl('import', 'import');

    const authorizationToken = localStorage.getItem('authorization_token');
    console.log(`Found token: ${authorizationToken}`);
    let headersAuth: any = {};
    if (authorizationToken) {
      headersAuth["Authorization"] =`Basic ${authorizationToken}`;
    }
    return this.http.get<PreSignedUrl>(url, {
      params: {
        name: fileName,
      },
      headers: headersAuth
    });
  }
}
