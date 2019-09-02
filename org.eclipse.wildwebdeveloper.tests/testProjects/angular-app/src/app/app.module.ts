import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { AppComponentWithHtml } from './app.componentWithHtml'

@NgModule({
  declarations: [
    AppComponent,
    AppComponentWithHtml
  ],
  imports: [
    BrowserModule
  ],
  providers: [],
  bootstrap: [AppComponent, AppComponentWithHtml]
})
export class AppModule { }
