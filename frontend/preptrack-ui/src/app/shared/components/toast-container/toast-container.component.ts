import { Component } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'pt-toast-container',
  standalone: true,
  imports: [AsyncPipe],
  template: `
    <div class="toast-container">
      @for (toast of toastService.toasts$ | async; track toast.id) {
        <div class="toast" [class]="'toast-' + toast.type">
          <div class="toast-content">{{ toast.message }}</div>
          <button (click)="toastService.remove(toast.id)" class="toast-close">×</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 1.5rem;
      right: 1.5rem;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      pointer-events: none;
    }
    .toast {
      pointer-events: auto;
      min-width: 300px;
      padding: 1rem 1.25rem;
      border-radius: 8px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      color: var(--text-primary);
      font-size: 0.875rem;
      font-weight: 500;
      box-shadow: var(--shadow-lg);
      animation: slideIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      background-color: var(--bg-surface);
      border: 1px solid var(--border-muted);
    }
    .toast-error {
      border-left: 4px solid var(--color-danger);
    }
    .toast-success {
      border-left: 4px solid var(--color-success);
    }
    .toast-warning {
      border-left: 4px solid var(--color-warning);
    }
    .toast-info {
      border-left: 4px solid var(--color-primary);
    }
    .toast-content {
      flex: 1;
    }
    .toast-close {
      background: none;
      border: none;
      color: var(--text-secondary);
      font-size: 1.5rem;
      cursor: pointer;
      padding: 0;
      margin-left: 1rem;
      line-height: 1;
      display: flex;
      align-items: center;
    }
    .toast-close:hover {
      color: #fff;
    }
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  `]
})
export class ToastContainerComponent {
  constructor(public toastService: ToastService) {}
}
