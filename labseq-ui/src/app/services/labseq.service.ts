import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LabseqResponse { index: number; value: string; }

@Injectable({ providedIn: 'root' })
export class LabseqService {
  private http = inject(HttpClient);
  getValue(n: number): Observable<LabseqResponse> {
    return this.http.get<LabseqResponse>(`/labseq/${n}`);
  }
}
