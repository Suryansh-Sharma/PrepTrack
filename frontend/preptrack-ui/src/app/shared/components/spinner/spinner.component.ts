import { Component, Input } from '@angular/core';

@Component({
  selector: 'pt-spinner',
  standalone: true,
  template: `
    <div class="spinner" [style.width.px]="size" [style.height.px]="size" [style.border-width.px]="thickness"></div>
  `,
  styles: [`
    .spinner {
      border-style: solid;
      border-color: rgba(255, 255, 255, 0.2);
      border-top-color: #ffffff;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      display: inline-block;
    }
    
    @keyframes spin {
      to {
        transform: rotate(360deg);
      }
    }
  `]
})
export class SpinnerComponent {
  @Input() size: number = 20;
  @Input() thickness: number = 3;
}
