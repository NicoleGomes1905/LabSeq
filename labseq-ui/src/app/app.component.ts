import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [FormsModule, NgIf],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent {
  readonly MAX_INDEX = 650000;

  n: number | null = 10;
  result: string | null = null;
  error: string | null = null;
  loading = false;

  constructor(private http: HttpClient) {}

  submit(): void {
    this.calculate();
  }

  onSubmit(): void {
    this.submit();
  }

  private calculate(): void {
    const idx = Number(this.n);
    this.error = null;
    this.result = null;

    if (!Number.isInteger(idx) || idx < 0) {
      this.error = 'Please enter a non-negative integer n.';
      return;
    }

    if (idx > this.MAX_INDEX) {
      this.error = `The maximum allowed value for n is ${this.MAX_INDEX}.`;
      return;
    }

    this.loading = true;
    this.http
      .get<{ index: number; value: string }>(`/labseq/${idx}`)
      .subscribe({
        next: (r) => {
          this.result = r.value;
          this.loading = false;
        },
        error: () => {
          this.error = 'Failed to call the Labseq service.';
          this.loading = false;
        },
      });
  }
}
