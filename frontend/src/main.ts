import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { LOCALE_ID } from '@angular/core';
import { AppComponent } from './app/app.component';
import { errorInterceptor } from './app/interceptors/error.interceptor';

registerLocaleData(localeFr);

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(withInterceptors([errorInterceptor])),
    { provide: LOCALE_ID, useValue: 'fr-FR' }
  ]
}).catch((err) => console.error(err));
