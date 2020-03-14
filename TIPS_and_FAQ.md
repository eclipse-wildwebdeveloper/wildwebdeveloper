# Wild Web Developer FAQ

### As a user, how can I...

#### Automatically compile TypeScript to JavaScript ?

(From https://github.com/eclipse/wildwebdeveloper/issues/331#issuecomment-577240880 )

1. Right-click on project > Properties > Builders
2. Click "New", select "Program"
3. location => path to tsc (can be `${system_path:tsc}`; working directory => project directory (can be `${project_loc}`); arguments...
4. In Build Options
  a. select "Launch in background"
  b. select "During auto-build"
  c. select the interesting typescript source folder in "Select working set of relevant resources" to include the source folder.
5. in Refresh, select what needs to be refreshed upon build.

#### Debug a website developed in TypeScript?

1. Ensure the `sourceMap` property of your `tsconfig.json` is set to `true`.
Example `tsconfig.json`:
```JSON
{
  "compilerOptions": {
    "target": "es5",
    "module": "commonjs",
    "outDir": "out",
    "sourceMap": true
  }
}
```
2. Run `tsc` to transpile your TypeScript source code to JavaScript.
3. Right click on the entry point of your website (eg. `index.html`) => Debug as => Chrome Debug
4. If a breakpoint is set in your `.ts` source file, it will be hit when the equivalent JavaScript code is run.

#### Debug a client-side code of a web app?

1. Start your web app's Node.JS web server (usually done through a NPM script in your package.json, eg. `npm start` ). This can be done through the commandline or by right-clicking on a package.json in the Project Explorer => Run as => NPM...
2. Once your web app is running, take note of the local URL it's running on. For React project's, it's by default `http://localhost:3000/`
3. Right click on your project's root directory in the Project Explorer => Debug as => Debug configurations...
4. Create a launch or attach debug configuration for Chrome or Firefox, depending on your preference.
5. For launch debug configurations, select the URL radio button and enter the URL from step 2. For attach debug configurations, simply enter the URL from step 2.
6. Ensure the working directory is correctly set to your project's root folder.
7. Click `apply` followed by `debug` in the bottom right corner of the debug configuration

#### Get instant HTML preview on save ?

1. Open the HTML file with the Generic Editor for edition.
2. From the Edition context menu, the Project Explorer or other explorer, open this same HTML file with the Internal Web Browser.
3. Drag the editor/browser to get them side by side or stacked one on top of the other in the IDE.
4. In the Web Browser, click the arrow besides the refresh button, and select "Auto-refersh local changes"
