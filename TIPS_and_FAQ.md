# Wild Web Developer FAQ

### As a user, how can I...

#### Automatically compile JavaScript to TypeScript?

(From https://github.com/eclipse/wildwebdeveloper/issues/331#issuecomment-577240880 )

1. Right-click on project > Properties > Builders
2. Click "New", select "Program"
3. location => path to tsc (can be `${system_path:tsc}`; working directory => project directory (can be `${project_loc}`); arguments...
4. In Build Options
  a. select "Launch in background"
  b. select "During auto-build"
  c. select the interesting typescript source folder in "Select working set of relevant resources" to include the source folder.
5. in Refresh, select what needs to be refreshed upon build.

#### Get instant HTML preview on save?

1. Open the HTML file with the Generic Editor for edition.
2. From the Edition context menu, the Project Explorer or other explorer, open this same HTML file with the Internal Web Browser.
3. Drag the editor/browser to get them side by side or stacked one on top of the other in the IDE.
4. In the Web Browser, click the arrow besides the refresh button, and select "Auto-refersh local changes"