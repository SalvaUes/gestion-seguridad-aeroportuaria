// vite.generated.ts
import path from "path";
import { existsSync as existsSync5, mkdirSync as mkdirSync2, readdirSync as readdirSync2, readFileSync as readFileSync4, writeFileSync as writeFileSync2 } from "fs";
import { createHash } from "crypto";
import * as net from "net";

// target/plugins/application-theme-plugin/theme-handle.js
import { existsSync as existsSync3, readFileSync as readFileSync2 } from "fs";
import { resolve as resolve3 } from "path";

// target/plugins/application-theme-plugin/theme-generator.js
import { globSync as globSync2 } from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/glob/dist/esm/index.js";
import { resolve as resolve2, basename as basename2 } from "path";
import { existsSync as existsSync2, readFileSync, writeFileSync } from "fs";

// target/plugins/application-theme-plugin/theme-copy.js
import { readdirSync, statSync, mkdirSync, existsSync, copyFileSync } from "fs";
import { resolve, basename, relative, extname } from "path";
import { globSync } from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/glob/dist/esm/index.js";
var ignoredFileExtensions = [".css", ".js", ".json"];
function copyThemeResources(themeFolder2, projectStaticAssetsOutputFolder, logger) {
  const staticAssetsThemeFolder = resolve(projectStaticAssetsOutputFolder, "themes", basename(themeFolder2));
  const collection = collectFolders(themeFolder2, logger);
  if (collection.files.length > 0) {
    mkdirSync(staticAssetsThemeFolder, { recursive: true });
    collection.directories.forEach((directory) => {
      const relativeDirectory = relative(themeFolder2, directory);
      const targetDirectory = resolve(staticAssetsThemeFolder, relativeDirectory);
      mkdirSync(targetDirectory, { recursive: true });
    });
    collection.files.forEach((file) => {
      const relativeFile = relative(themeFolder2, file);
      const targetFile = resolve(staticAssetsThemeFolder, relativeFile);
      copyFileIfAbsentOrNewer(file, targetFile, logger);
    });
  }
}
function collectFolders(folderToCopy, logger) {
  const collection = { directories: [], files: [] };
  logger.trace("files in directory", readdirSync(folderToCopy));
  readdirSync(folderToCopy).forEach((file) => {
    const fileToCopy = resolve(folderToCopy, file);
    try {
      if (statSync(fileToCopy).isDirectory()) {
        logger.debug("Going through directory", fileToCopy);
        const result = collectFolders(fileToCopy, logger);
        if (result.files.length > 0) {
          collection.directories.push(fileToCopy);
          logger.debug("Adding directory", fileToCopy);
          collection.directories.push.apply(collection.directories, result.directories);
          collection.files.push.apply(collection.files, result.files);
        }
      } else if (!ignoredFileExtensions.includes(extname(fileToCopy))) {
        logger.debug("Adding file", fileToCopy);
        collection.files.push(fileToCopy);
      }
    } catch (error) {
      handleNoSuchFileError(fileToCopy, error, logger);
    }
  });
  return collection;
}
function copyStaticAssets(themeName, themeProperties, projectStaticAssetsOutputFolder, logger) {
  const assets = themeProperties["assets"];
  if (!assets) {
    logger.debug("no assets to handle no static assets were copied");
    return;
  }
  mkdirSync(projectStaticAssetsOutputFolder, {
    recursive: true
  });
  const missingModules = checkModules(Object.keys(assets));
  if (missingModules.length > 0) {
    throw Error(
      "Missing npm modules '" + missingModules.join("', '") + "' for assets marked in 'theme.json'.\nInstall package(s) by adding a @NpmPackage annotation or install it using 'npm/pnpm/bun i'"
    );
  }
  Object.keys(assets).forEach((module) => {
    const copyRules = assets[module];
    Object.keys(copyRules).forEach((copyRule) => {
      const nodeSources = resolve("node_modules/", module, copyRule);
      const files = globSync(nodeSources, { nodir: true });
      const targetFolder = resolve(projectStaticAssetsOutputFolder, "themes", themeName, copyRules[copyRule]);
      mkdirSync(targetFolder, {
        recursive: true
      });
      files.forEach((file) => {
        const copyTarget = resolve(targetFolder, basename(file));
        copyFileIfAbsentOrNewer(file, copyTarget, logger);
      });
    });
  });
}
function checkModules(modules) {
  const missing = [];
  modules.forEach((module) => {
    if (!existsSync(resolve("node_modules/", module))) {
      missing.push(module);
    }
  });
  return missing;
}
function copyFileIfAbsentOrNewer(fileToCopy, copyTarget, logger) {
  try {
    if (!existsSync(copyTarget) || statSync(copyTarget).mtime < statSync(fileToCopy).mtime) {
      logger.trace("Copying: ", fileToCopy, "=>", copyTarget);
      copyFileSync(fileToCopy, copyTarget);
    }
  } catch (error) {
    handleNoSuchFileError(fileToCopy, error, logger);
  }
}
function handleNoSuchFileError(file, error, logger) {
  if (error.code === "ENOENT") {
    logger.warn("Ignoring not existing file " + file + ". File may have been deleted during theme processing.");
  } else {
    throw error;
  }
}

// target/plugins/application-theme-plugin/theme-generator.js
var themeComponentsFolder = "components";
var documentCssFilename = "document.css";
var stylesCssFilename = "styles.css";
var CSSIMPORT_COMMENT = "CSSImport end";
var headerImport = `import 'construct-style-sheets-polyfill';
`;
function writeThemeFiles(themeFolder2, themeName, themeProperties, options) {
  const productionMode = !options.devMode;
  const useDevServerOrInProductionMode = !options.useDevBundle;
  const outputFolder = options.frontendGeneratedFolder;
  const styles = resolve2(themeFolder2, stylesCssFilename);
  const documentCssFile = resolve2(themeFolder2, documentCssFilename);
  const autoInjectComponents = themeProperties.autoInjectComponents ?? true;
  const globalFilename = "theme-" + themeName + ".global.generated.js";
  const componentsFilename = "theme-" + themeName + ".components.generated.js";
  const themeFilename = "theme-" + themeName + ".generated.js";
  let themeFileContent = headerImport;
  let globalImportContent = "// When this file is imported, global styles are automatically applied\n";
  let componentsFileContent = "";
  var componentsFiles;
  if (autoInjectComponents) {
    componentsFiles = globSync2("*.css", {
      cwd: resolve2(themeFolder2, themeComponentsFolder),
      nodir: true
    });
    if (componentsFiles.length > 0) {
      componentsFileContent += "import { unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin/register-styles';\n";
    }
  }
  if (themeProperties.parent) {
    themeFileContent += `import { applyTheme as applyBaseTheme } from './theme-${themeProperties.parent}.generated.js';
`;
  }
  themeFileContent += `import { injectGlobalCss } from 'Frontend/generated/jar-resources/theme-util.js';
`;
  themeFileContent += `import './${componentsFilename}';
`;
  themeFileContent += `let needsReloadOnChanges = false;
`;
  const imports = [];
  const componentCssImports = [];
  const globalFileContent = [];
  const globalCssCode = [];
  const shadowOnlyCss = [];
  const componentCssCode = [];
  const parentTheme = themeProperties.parent ? "applyBaseTheme(target);\n" : "";
  const parentThemeGlobalImport = themeProperties.parent ? `import './theme-${themeProperties.parent}.global.generated.js';
` : "";
  const themeIdentifier = "_vaadintheme_" + themeName + "_";
  const lumoCssFlag = "_vaadinthemelumoimports_";
  const globalCssFlag = themeIdentifier + "globalCss";
  const componentCssFlag = themeIdentifier + "componentCss";
  if (!existsSync2(styles)) {
    if (productionMode) {
      throw new Error(`styles.css file is missing and is needed for '${themeName}' in folder '${themeFolder2}'`);
    }
    writeFileSync(
      styles,
      "/* Import your application global css files here or add the styles directly to this file */",
      "utf8"
    );
  }
  let filename = basename2(styles);
  let variable = camelCase(filename);
  const lumoImports = themeProperties.lumoImports || ["color", "typography"];
  if (lumoImports) {
    lumoImports.forEach((lumoImport) => {
      imports.push(`import { ${lumoImport} } from '@vaadin/vaadin-lumo-styles/${lumoImport}.js';
`);
      if (lumoImport === "utility" || lumoImport === "badge" || lumoImport === "typography" || lumoImport === "color") {
        globalFileContent.push(`import '@vaadin/vaadin-lumo-styles/${lumoImport}-global.js';
`);
      }
    });
    lumoImports.forEach((lumoImport) => {
      shadowOnlyCss.push(`removers.push(injectGlobalCss(${lumoImport}.cssText, '', target, true));
`);
    });
  }
  if (useDevServerOrInProductionMode) {
    globalFileContent.push(parentThemeGlobalImport);
    globalFileContent.push(`import 'themes/${themeName}/${filename}';
`);
    imports.push(`import ${variable} from 'themes/${themeName}/${filename}?inline';
`);
    shadowOnlyCss.push(`removers.push(injectGlobalCss(${variable}.toString(), '', target));
    `);
  }
  if (existsSync2(documentCssFile)) {
    filename = basename2(documentCssFile);
    variable = camelCase(filename);
    if (useDevServerOrInProductionMode) {
      globalFileContent.push(`import 'themes/${themeName}/${filename}';
`);
      imports.push(`import ${variable} from 'themes/${themeName}/${filename}?inline';
`);
      shadowOnlyCss.push(`removers.push(injectGlobalCss(${variable}.toString(),'', document));
    `);
    }
  }
  let i = 0;
  if (themeProperties.documentCss) {
    const missingModules = checkModules(themeProperties.documentCss);
    if (missingModules.length > 0) {
      throw Error(
        "Missing npm modules or files '" + missingModules.join("', '") + "' for documentCss marked in 'theme.json'.\nInstall or update package(s) by adding a @NpmPackage annotation or install it using 'npm/pnpm/bun i'"
      );
    }
    themeProperties.documentCss.forEach((cssImport) => {
      const variable2 = "module" + i++;
      imports.push(`import ${variable2} from '${cssImport}?inline';
`);
      globalCssCode.push(`if(target !== document) {
        removers.push(injectGlobalCss(${variable2}.toString(), '', target));
    }
    `);
      globalCssCode.push(
        `removers.push(injectGlobalCss(${variable2}.toString(), '${CSSIMPORT_COMMENT}', document));
    `
      );
    });
  }
  if (themeProperties.importCss) {
    const missingModules = checkModules(themeProperties.importCss);
    if (missingModules.length > 0) {
      throw Error(
        "Missing npm modules or files '" + missingModules.join("', '") + "' for importCss marked in 'theme.json'.\nInstall or update package(s) by adding a @NpmPackage annotation or install it using 'npm/pnpm/bun i'"
      );
    }
    themeProperties.importCss.forEach((cssPath) => {
      const variable2 = "module" + i++;
      globalFileContent.push(`import '${cssPath}';
`);
      imports.push(`import ${variable2} from '${cssPath}?inline';
`);
      shadowOnlyCss.push(`removers.push(injectGlobalCss(${variable2}.toString(), '${CSSIMPORT_COMMENT}', target));
`);
    });
  }
  if (autoInjectComponents) {
    componentsFiles.forEach((componentCss) => {
      const filename2 = basename2(componentCss);
      const tag = filename2.replace(".css", "");
      const variable2 = camelCase(filename2);
      componentCssImports.push(
        `import ${variable2} from 'themes/${themeName}/${themeComponentsFolder}/${filename2}?inline';
`
      );
      const componentString = `registerStyles(
        '${tag}',
        unsafeCSS(${variable2}.toString())
      );
      `;
      componentCssCode.push(componentString);
    });
  }
  themeFileContent += imports.join("");
  const themeFileApply = `
  let themeRemovers = new WeakMap();
  let targets = [];

  export const applyTheme = (target) => {
    const removers = [];
    if (target !== document) {
      ${shadowOnlyCss.join("")}
    }
    ${parentTheme}
    ${globalCssCode.join("")}

    if (import.meta.hot) {
      targets.push(new WeakRef(target));
      themeRemovers.set(target, removers);
    }

  }
  
`;
  componentsFileContent += `
${componentCssImports.join("")}

if (!document['${componentCssFlag}']) {
  ${componentCssCode.join("")}
  document['${componentCssFlag}'] = true;
}

if (import.meta.hot) {
  import.meta.hot.accept((module) => {
    window.location.reload();
  });
}

`;
  themeFileContent += themeFileApply;
  themeFileContent += `
if (import.meta.hot) {
  import.meta.hot.accept((module) => {

    if (needsReloadOnChanges) {
      window.location.reload();
    } else {
      targets.forEach(targetRef => {
        const target = targetRef.deref();
        if (target) {
          themeRemovers.get(target).forEach(remover => remover())
          module.applyTheme(target);
        }
      })
    }
  });

  import.meta.hot.on('vite:afterUpdate', (update) => {
    document.dispatchEvent(new CustomEvent('vaadin-theme-updated', { detail: update }));
  });
}

`;
  globalImportContent += `
${globalFileContent.join("")}
`;
  writeIfChanged(resolve2(outputFolder, globalFilename), globalImportContent);
  writeIfChanged(resolve2(outputFolder, themeFilename), themeFileContent);
  writeIfChanged(resolve2(outputFolder, componentsFilename), componentsFileContent);
}
function writeIfChanged(file, data) {
  if (!existsSync2(file) || readFileSync(file, { encoding: "utf-8" }) !== data) {
    writeFileSync(file, data);
  }
}
function camelCase(str) {
  return str.replace(/(?:^\w|[A-Z]|\b\w)/g, function(word, index) {
    return index === 0 ? word.toLowerCase() : word.toUpperCase();
  }).replace(/\s+/g, "").replace(/\.|\-/g, "");
}

// target/plugins/application-theme-plugin/theme-handle.js
var nameRegex = /theme-(.*)\.generated\.js/;
var prevThemeName = void 0;
var firstThemeName = void 0;
function processThemeResources(options, logger) {
  const themeName = extractThemeName(options.frontendGeneratedFolder);
  if (themeName) {
    if (!prevThemeName && !firstThemeName) {
      firstThemeName = themeName;
    } else if (prevThemeName && prevThemeName !== themeName && firstThemeName !== themeName || !prevThemeName && firstThemeName !== themeName) {
      const warning = `Attention: Active theme is switched to '${themeName}'.`;
      const description = `
      Note that adding new style sheet files to '/themes/${themeName}/components', 
      may not be taken into effect until the next application restart.
      Changes to already existing style sheet files are being reloaded as before.`;
      logger.warn("*******************************************************************");
      logger.warn(warning);
      logger.warn(description);
      logger.warn("*******************************************************************");
    }
    prevThemeName = themeName;
    findThemeFolderAndHandleTheme(themeName, options, logger);
  } else {
    prevThemeName = void 0;
    logger.debug("Skipping Vaadin application theme handling.");
    logger.trace("Most likely no @Theme annotation for application or only themeClass used.");
  }
}
function findThemeFolderAndHandleTheme(themeName, options, logger) {
  let themeFound = false;
  for (let i = 0; i < options.themeProjectFolders.length; i++) {
    const themeProjectFolder = options.themeProjectFolders[i];
    if (existsSync3(themeProjectFolder)) {
      logger.debug("Searching themes folder '" + themeProjectFolder + "' for theme '" + themeName + "'");
      const handled = handleThemes(themeName, themeProjectFolder, options, logger);
      if (handled) {
        if (themeFound) {
          throw new Error(
            "Found theme files in '" + themeProjectFolder + "' and '" + themeFound + "'. Theme should only be available in one folder"
          );
        }
        logger.debug("Found theme files from '" + themeProjectFolder + "'");
        themeFound = themeProjectFolder;
      }
    }
  }
  if (existsSync3(options.themeResourceFolder)) {
    if (themeFound && existsSync3(resolve3(options.themeResourceFolder, themeName))) {
      throw new Error(
        "Theme '" + themeName + `'should not exist inside a jar and in the project at the same time
Extending another theme is possible by adding { "parent": "my-parent-theme" } entry to the theme.json file inside your theme folder.`
      );
    }
    logger.debug(
      "Searching theme jar resource folder '" + options.themeResourceFolder + "' for theme '" + themeName + "'"
    );
    handleThemes(themeName, options.themeResourceFolder, options, logger);
    themeFound = true;
  }
  return themeFound;
}
function handleThemes(themeName, themesFolder, options, logger) {
  const themeFolder2 = resolve3(themesFolder, themeName);
  if (existsSync3(themeFolder2)) {
    logger.debug("Found theme ", themeName, " in folder ", themeFolder2);
    const themeProperties = getThemeProperties(themeFolder2);
    if (themeProperties.parent) {
      const found = findThemeFolderAndHandleTheme(themeProperties.parent, options, logger);
      if (!found) {
        throw new Error(
          "Could not locate files for defined parent theme '" + themeProperties.parent + "'.\nPlease verify that dependency is added or theme folder exists."
        );
      }
    }
    copyStaticAssets(themeName, themeProperties, options.projectStaticAssetsOutputFolder, logger);
    copyThemeResources(themeFolder2, options.projectStaticAssetsOutputFolder, logger);
    writeThemeFiles(themeFolder2, themeName, themeProperties, options);
    return true;
  }
  return false;
}
function getThemeProperties(themeFolder2) {
  const themePropertyFile = resolve3(themeFolder2, "theme.json");
  if (!existsSync3(themePropertyFile)) {
    return {};
  }
  const themePropertyFileAsString = readFileSync2(themePropertyFile);
  if (themePropertyFileAsString.length === 0) {
    return {};
  }
  return JSON.parse(themePropertyFileAsString);
}
function extractThemeName(frontendGeneratedFolder) {
  if (!frontendGeneratedFolder) {
    throw new Error(
      "Couldn't extract theme name from 'theme.js', because the path to folder containing this file is empty. Please set the a correct folder path in ApplicationThemePlugin constructor parameters."
    );
  }
  const generatedThemeFile = resolve3(frontendGeneratedFolder, "theme.js");
  if (existsSync3(generatedThemeFile)) {
    const themeName = nameRegex.exec(readFileSync2(generatedThemeFile, { encoding: "utf8" }))[1];
    if (!themeName) {
      throw new Error("Couldn't parse theme name from '" + generatedThemeFile + "'.");
    }
    return themeName;
  } else {
    return "";
  }
}

// target/plugins/theme-loader/theme-loader-utils.js
import { existsSync as existsSync4, readFileSync as readFileSync3 } from "fs";
import { resolve as resolve4, basename as basename3 } from "path";
import { globSync as globSync3 } from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/glob/dist/esm/index.js";
var urlMatcher = /(url\(\s*)(\'|\")?(\.\/|\.\.\/)((?:\3)*)?(\S*)(\2\s*\))/g;
function assetsContains(fileUrl, themeFolder2, logger) {
  const themeProperties = getThemeProperties2(themeFolder2);
  if (!themeProperties) {
    logger.debug("No theme properties found.");
    return false;
  }
  const assets = themeProperties["assets"];
  if (!assets) {
    logger.debug("No defined assets in theme properties");
    return false;
  }
  for (let module of Object.keys(assets)) {
    const copyRules = assets[module];
    for (let copyRule of Object.keys(copyRules)) {
      if (fileUrl.startsWith(copyRules[copyRule])) {
        const targetFile = fileUrl.replace(copyRules[copyRule], "");
        const files = globSync3(resolve4("node_modules/", module, copyRule), { nodir: true });
        for (let file of files) {
          if (file.endsWith(targetFile))
            return true;
        }
      }
    }
  }
  return false;
}
function getThemeProperties2(themeFolder2) {
  const themePropertyFile = resolve4(themeFolder2, "theme.json");
  if (!existsSync4(themePropertyFile)) {
    return {};
  }
  const themePropertyFileAsString = readFileSync3(themePropertyFile);
  if (themePropertyFileAsString.length === 0) {
    return {};
  }
  return JSON.parse(themePropertyFileAsString);
}
function rewriteCssUrls(source, handledResourceFolder, themeFolder2, logger, options) {
  source = source.replace(urlMatcher, function(match, url, quoteMark, replace2, additionalDotSegments, fileUrl, endString) {
    let absolutePath = resolve4(handledResourceFolder, replace2, additionalDotSegments || "", fileUrl);
    let existingThemeResource = absolutePath.startsWith(themeFolder2) && existsSync4(absolutePath);
    if (!existingThemeResource && additionalDotSegments) {
      absolutePath = resolve4(handledResourceFolder, replace2, fileUrl);
      existingThemeResource = absolutePath.startsWith(themeFolder2) && existsSync4(absolutePath);
    }
    const isAsset = assetsContains(fileUrl, themeFolder2, logger);
    if (existingThemeResource || isAsset) {
      const replacement = options.devMode ? "./" : "../static/";
      const skipLoader = existingThemeResource ? "" : replacement;
      const frontendThemeFolder = skipLoader + "themes/" + basename3(themeFolder2);
      logger.log(
        "Updating url for file",
        "'" + replace2 + fileUrl + "'",
        "to use",
        "'" + frontendThemeFolder + "/" + fileUrl + "'"
      );
      const pathResolved = isAsset ? "/" + fileUrl : absolutePath.substring(themeFolder2.length).replace(/\\/g, "/");
      return url + (quoteMark ?? "") + frontendThemeFolder + pathResolved + endString;
    } else if (options.devMode) {
      logger.log("No rewrite for '", match, "' as the file was not found.");
    } else {
      return url + (quoteMark ?? "") + "../../" + fileUrl + endString;
    }
    return match;
  });
  return source;
}

// target/plugins/react-function-location-plugin/react-function-location-plugin.js
import * as t from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/@babel/types/lib/index.js";
function addFunctionComponentSourceLocationBabel() {
  function isReactFunctionName(name) {
    return name && name.match(/^[A-Z].*/);
  }
  function addDebugInfo(path2, name, filename, loc) {
    const lineNumber = loc.start.line;
    const columnNumber = loc.start.column + 1;
    const debugSourceMember = t.memberExpression(t.identifier(name), t.identifier("__debugSourceDefine"));
    const debugSourceDefine = t.objectExpression([
      t.objectProperty(t.identifier("fileName"), t.stringLiteral(filename)),
      t.objectProperty(t.identifier("lineNumber"), t.numericLiteral(lineNumber)),
      t.objectProperty(t.identifier("columnNumber"), t.numericLiteral(columnNumber))
    ]);
    const assignment = t.expressionStatement(t.assignmentExpression("=", debugSourceMember, debugSourceDefine));
    const condition = t.binaryExpression(
      "===",
      t.unaryExpression("typeof", t.identifier(name)),
      t.stringLiteral("function")
    );
    const ifFunction = t.ifStatement(condition, t.blockStatement([assignment]));
    path2.insertAfter(ifFunction);
  }
  return {
    visitor: {
      VariableDeclaration(path2, state) {
        path2.node.declarations.forEach((declaration) => {
          if (declaration.id.type !== "Identifier") {
            return;
          }
          const name = declaration?.id?.name;
          if (!isReactFunctionName(name)) {
            return;
          }
          const filename = state.file.opts.filename;
          if (declaration?.init?.body?.loc) {
            addDebugInfo(path2, name, filename, declaration.init.body.loc);
          }
        });
      },
      FunctionDeclaration(path2, state) {
        const node = path2.node;
        const name = node?.id?.name;
        if (!isReactFunctionName(name)) {
          return;
        }
        const filename = state.file.opts.filename;
        addDebugInfo(path2, name, filename, node.body.loc);
      }
    }
  };
}

// target/vaadin-dev-server-settings.json
var vaadin_dev_server_settings_default = {
  frontendFolder: "F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/./src/main/frontend",
  themeFolder: "themes",
  themeResourceFolder: "F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/./src/main/frontend/generated/jar-resources",
  staticOutput: "F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/target/classes/META-INF/VAADIN/webapp/VAADIN/static",
  generatedFolder: "generated",
  statsOutput: "F:\\Proyecto-Seguridad\\gestion-seguridad-aeroportuaria\\target\\classes\\META-INF\\VAADIN\\config",
  frontendBundleOutput: "F:\\Proyecto-Seguridad\\gestion-seguridad-aeroportuaria\\target\\classes\\META-INF\\VAADIN\\webapp",
  devBundleOutput: "F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/target/dev-bundle/webapp",
  devBundleStatsOutput: "F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/target/dev-bundle/config",
  jarResourcesFolder: "F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/./src/main/frontend/generated/jar-resources",
  themeName: "gestionseguridadaeroportuaria",
  clientServiceWorkerSource: "F:\\Proyecto-Seguridad\\gestion-seguridad-aeroportuaria\\target\\sw.ts",
  pwaEnabled: false,
  offlineEnabled: false,
  offlinePath: "'offline.html'"
};

// vite.generated.ts
import {
  defineConfig,
  mergeConfig
} from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/vite/dist/node/index.js";
import { getManifest } from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/workbox-build/build/index.js";
import * as rollup from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/rollup/dist/es/rollup.js";
import brotli from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/rollup-plugin-brotli/lib/index.cjs.js";
import replace from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/@rollup/plugin-replace/dist/es/index.js";
import checker from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/vite-plugin-checker/dist/esm/main.js";

// target/plugins/rollup-plugin-postcss-lit-custom/rollup-plugin-postcss-lit.js
import { createFilter } from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/@rollup/pluginutils/dist/es/index.js";
import transformAst from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/transform-ast/index.js";
var assetUrlRE = /__VITE_ASSET__([\w$]+)__(?:\$_(.*?)__)?/g;
var escape = (str) => str.replace(assetUrlRE, '${unsafeCSSTag("__VITE_ASSET__$1__$2")}').replace(/`/g, "\\`").replace(/\\(?!`)/g, "\\\\");
function postcssLit(options = {}) {
  const defaultOptions = {
    include: "**/*.{css,sss,pcss,styl,stylus,sass,scss,less}",
    exclude: null,
    importPackage: "lit"
  };
  const opts = { ...defaultOptions, ...options };
  const filter = createFilter(opts.include, opts.exclude);
  return {
    name: "postcss-lit",
    enforce: "post",
    transform(code, id) {
      if (!filter(id))
        return;
      const ast = this.parse(code, {});
      let defaultExportName;
      let isDeclarationLiteral = false;
      const magicString = transformAst(code, { ast }, (node) => {
        if (node.type === "ExportDefaultDeclaration") {
          defaultExportName = node.declaration.name;
          isDeclarationLiteral = node.declaration.type === "Literal";
        }
      });
      if (!defaultExportName && !isDeclarationLiteral) {
        return;
      }
      magicString.walk((node) => {
        if (defaultExportName && node.type === "VariableDeclaration") {
          const exportedVar = node.declarations.find((d) => d.id.name === defaultExportName);
          if (exportedVar) {
            exportedVar.init.edit.update(`cssTag\`${escape(exportedVar.init.value)}\``);
          }
        }
        if (isDeclarationLiteral && node.type === "ExportDefaultDeclaration") {
          node.declaration.edit.update(`cssTag\`${escape(node.declaration.value)}\``);
        }
      });
      magicString.prepend(`import {css as cssTag, unsafeCSS as unsafeCSSTag} from '${opts.importPackage}';
`);
      return {
        code: magicString.toString(),
        map: magicString.generateMap({
          hires: true
        })
      };
    }
  };
}

// vite.generated.ts
import { createRequire } from "module";
import { visualizer } from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/rollup-plugin-visualizer/dist/plugin/index.js";
import reactPlugin from "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/node_modules/@vitejs/plugin-react/dist/index.mjs";
var __vite_injected_original_dirname = "F:\\Proyecto-Seguridad\\gestion-seguridad-aeroportuaria";
var __vite_injected_original_import_meta_url = "file:///F:/Proyecto-Seguridad/gestion-seguridad-aeroportuaria/vite.generated.ts";
var require2 = createRequire(__vite_injected_original_import_meta_url);
var appShellUrl = ".";
var frontendFolder = path.resolve(__vite_injected_original_dirname, vaadin_dev_server_settings_default.frontendFolder);
var themeFolder = path.resolve(frontendFolder, vaadin_dev_server_settings_default.themeFolder);
var frontendBundleFolder = path.resolve(__vite_injected_original_dirname, vaadin_dev_server_settings_default.frontendBundleOutput);
var devBundleFolder = path.resolve(__vite_injected_original_dirname, vaadin_dev_server_settings_default.devBundleOutput);
var devBundle = !!process.env.devBundle;
var jarResourcesFolder = path.resolve(__vite_injected_original_dirname, vaadin_dev_server_settings_default.jarResourcesFolder);
var themeResourceFolder = path.resolve(__vite_injected_original_dirname, vaadin_dev_server_settings_default.themeResourceFolder);
var projectPackageJsonFile = path.resolve(__vite_injected_original_dirname, "package.json");
var buildOutputFolder = devBundle ? devBundleFolder : frontendBundleFolder;
var statsFolder = path.resolve(__vite_injected_original_dirname, devBundle ? vaadin_dev_server_settings_default.devBundleStatsOutput : vaadin_dev_server_settings_default.statsOutput);
var statsFile = path.resolve(statsFolder, "stats.json");
var bundleSizeFile = path.resolve(statsFolder, "bundle-size.html");
var nodeModulesFolder = path.resolve(__vite_injected_original_dirname, "node_modules");
var webComponentTags = "";
var projectIndexHtml = path.resolve(frontendFolder, "index.html");
var projectStaticAssetsFolders = [
  path.resolve(__vite_injected_original_dirname, "src", "main", "resources", "META-INF", "resources"),
  path.resolve(__vite_injected_original_dirname, "src", "main", "resources", "static"),
  frontendFolder
];
var themeProjectFolders = projectStaticAssetsFolders.map((folder) => path.resolve(folder, vaadin_dev_server_settings_default.themeFolder));
var themeOptions = {
  devMode: false,
  useDevBundle: devBundle,
  // The following matches folder 'frontend/generated/themes/'
  // (not 'frontend/themes') for theme in JAR that is copied there
  themeResourceFolder: path.resolve(themeResourceFolder, vaadin_dev_server_settings_default.themeFolder),
  themeProjectFolders,
  projectStaticAssetsOutputFolder: devBundle ? path.resolve(devBundleFolder, "../assets") : path.resolve(__vite_injected_original_dirname, vaadin_dev_server_settings_default.staticOutput),
  frontendGeneratedFolder: path.resolve(frontendFolder, vaadin_dev_server_settings_default.generatedFolder)
};
var hasExportedWebComponents = existsSync5(path.resolve(frontendFolder, "web-component.html"));
console.trace = () => {
};
console.debug = () => {
};
function injectManifestToSWPlugin() {
  const rewriteManifestIndexHtmlUrl = (manifest) => {
    const indexEntry = manifest.find((entry) => entry.url === "index.html");
    if (indexEntry) {
      indexEntry.url = appShellUrl;
    }
    return { manifest, warnings: [] };
  };
  return {
    name: "vaadin:inject-manifest-to-sw",
    async transform(code, id) {
      if (/sw\.(ts|js)$/.test(id)) {
        const { manifestEntries } = await getManifest({
          globDirectory: buildOutputFolder,
          globPatterns: ["**/*"],
          globIgnores: ["**/*.br"],
          manifestTransforms: [rewriteManifestIndexHtmlUrl],
          maximumFileSizeToCacheInBytes: 100 * 1024 * 1024
          // 100mb,
        });
        return code.replace("self.__WB_MANIFEST", JSON.stringify(manifestEntries));
      }
    }
  };
}
function buildSWPlugin(opts) {
  let config;
  const devMode = opts.devMode;
  const swObj = {};
  async function build(action, additionalPlugins = []) {
    const includedPluginNames = [
      "vite:esbuild",
      "rollup-plugin-dynamic-import-variables",
      "vite:esbuild-transpile",
      "vite:terser"
    ];
    const plugins = config.plugins.filter((p) => {
      return includedPluginNames.includes(p.name);
    });
    const resolver = config.createResolver();
    const resolvePlugin = {
      name: "resolver",
      resolveId(source, importer, _options) {
        return resolver(source, importer);
      }
    };
    plugins.unshift(resolvePlugin);
    plugins.push(
      replace({
        values: {
          "process.env.NODE_ENV": JSON.stringify(config.mode),
          ...config.define
        },
        preventAssignment: true
      })
    );
    if (additionalPlugins) {
      plugins.push(...additionalPlugins);
    }
    const bundle = await rollup.rollup({
      input: path.resolve(vaadin_dev_server_settings_default.clientServiceWorkerSource),
      plugins
    });
    try {
      return await bundle[action]({
        file: path.resolve(buildOutputFolder, "sw.js"),
        format: "es",
        exports: "none",
        sourcemap: config.command === "serve" || config.build.sourcemap,
        inlineDynamicImports: true
      });
    } finally {
      await bundle.close();
    }
  }
  return {
    name: "vaadin:build-sw",
    enforce: "post",
    async configResolved(resolvedConfig) {
      config = resolvedConfig;
    },
    async buildStart() {
      if (devMode) {
        const { output } = await build("generate");
        swObj.code = output[0].code;
        swObj.map = output[0].map;
      }
    },
    async load(id) {
      if (id.endsWith("sw.js")) {
        return "";
      }
    },
    async transform(_code, id) {
      if (id.endsWith("sw.js")) {
        return swObj;
      }
    },
    async closeBundle() {
      if (!devMode) {
        await build("write", [injectManifestToSWPlugin(), brotli()]);
      }
    }
  };
}
function statsExtracterPlugin() {
  function collectThemeJsonsInFrontend(themeJsonContents, themeName) {
    const themeJson = path.resolve(frontendFolder, vaadin_dev_server_settings_default.themeFolder, themeName, "theme.json");
    if (existsSync5(themeJson)) {
      const themeJsonContent = readFileSync4(themeJson, { encoding: "utf-8" }).replace(/\r\n/g, "\n");
      themeJsonContents[themeName] = themeJsonContent;
      const themeJsonObject = JSON.parse(themeJsonContent);
      if (themeJsonObject.parent) {
        collectThemeJsonsInFrontend(themeJsonContents, themeJsonObject.parent);
      }
    }
  }
  return {
    name: "vaadin:stats",
    enforce: "post",
    async writeBundle(options, bundle) {
      const modules = Object.values(bundle).flatMap((b) => b.modules ? Object.keys(b.modules) : []);
      const nodeModulesFolders = modules.map((id) => id.replace(/\\/g, "/")).filter((id) => id.startsWith(nodeModulesFolder.replace(/\\/g, "/"))).map((id) => id.substring(nodeModulesFolder.length + 1));
      const npmModules = nodeModulesFolders.map((id) => id.replace(/\\/g, "/")).map((id) => {
        const parts = id.split("/");
        if (id.startsWith("@")) {
          return parts[0] + "/" + parts[1];
        } else {
          return parts[0];
        }
      }).sort().filter((value, index, self) => self.indexOf(value) === index);
      const npmModuleAndVersion = Object.fromEntries(npmModules.map((module) => [module, getVersion(module)]));
      const cvdls = Object.fromEntries(
        npmModules.filter((module) => getCvdlName(module) != null).map((module) => [module, { name: getCvdlName(module), version: getVersion(module) }])
      );
      mkdirSync2(path.dirname(statsFile), { recursive: true });
      const projectPackageJson = JSON.parse(readFileSync4(projectPackageJsonFile, { encoding: "utf-8" }));
      const entryScripts = Object.values(bundle).filter((bundle2) => bundle2.isEntry).map((bundle2) => bundle2.fileName);
      const generatedIndexHtml = path.resolve(buildOutputFolder, "index.html");
      const customIndexData = readFileSync4(projectIndexHtml, { encoding: "utf-8" });
      const generatedIndexData = readFileSync4(generatedIndexHtml, {
        encoding: "utf-8"
      });
      const customIndexRows = new Set(customIndexData.split(/[\r\n]/).filter((row) => row.trim() !== ""));
      const generatedIndexRows = generatedIndexData.split(/[\r\n]/).filter((row) => row.trim() !== "");
      const rowsGenerated = [];
      generatedIndexRows.forEach((row) => {
        if (!customIndexRows.has(row)) {
          rowsGenerated.push(row);
        }
      });
      const parseImports = (filename, result) => {
        const content = readFileSync4(filename, { encoding: "utf-8" });
        const lines = content.split("\n");
        const staticImports = lines.filter((line) => line.startsWith("import ")).map((line) => line.substring(line.indexOf("'") + 1, line.lastIndexOf("'"))).map((line) => line.includes("?") ? line.substring(0, line.lastIndexOf("?")) : line);
        const dynamicImports = lines.filter((line) => line.includes("import(")).map((line) => line.replace(/.*import\(/, "")).map((line) => line.split(/'/)[1]).map((line) => line.includes("?") ? line.substring(0, line.lastIndexOf("?")) : line);
        staticImports.forEach((staticImport) => result.add(staticImport));
        dynamicImports.map((dynamicImport) => {
          const importedFile = path.resolve(path.dirname(filename), dynamicImport);
          parseImports(importedFile, result);
        });
      };
      const generatedImportsSet = /* @__PURE__ */ new Set();
      parseImports(
        path.resolve(themeOptions.frontendGeneratedFolder, "flow", "generated-flow-imports.js"),
        generatedImportsSet
      );
      const generatedImports = Array.from(generatedImportsSet).sort();
      const frontendFiles = {};
      const projectFileExtensions = [".js", ".js.map", ".ts", ".ts.map", ".tsx", ".tsx.map", ".css", ".css.map"];
      const isThemeComponentsResource = (id) => id.startsWith(themeOptions.frontendGeneratedFolder.replace(/\\/g, "/")) && id.match(/.*\/jar-resources\/themes\/[^\/]+\/components\//);
      const isGeneratedWebComponentResource = (id) => id.startsWith(themeOptions.frontendGeneratedFolder.replace(/\\/g, "/")) && id.match(/.*\/flow\/web-components\//);
      const isFrontendResourceCollected = (id) => !id.startsWith(themeOptions.frontendGeneratedFolder.replace(/\\/g, "/")) || isThemeComponentsResource(id) || isGeneratedWebComponentResource(id);
      modules.map((id) => id.replace(/\\/g, "/")).filter((id) => id.startsWith(frontendFolder.replace(/\\/g, "/"))).filter(isFrontendResourceCollected).map((id) => id.substring(frontendFolder.length + 1)).map((line) => line.includes("?") ? line.substring(0, line.lastIndexOf("?")) : line).forEach((line) => {
        const filePath = path.resolve(frontendFolder, line);
        if (projectFileExtensions.includes(path.extname(filePath))) {
          const fileBuffer = readFileSync4(filePath, { encoding: "utf-8" }).replace(/\r\n/g, "\n");
          frontendFiles[line] = createHash("sha256").update(fileBuffer, "utf8").digest("hex");
        }
      });
      generatedImports.filter((line) => line.includes("generated/jar-resources")).forEach((line) => {
        let filename = line.substring(line.indexOf("generated"));
        const fileBuffer = readFileSync4(path.resolve(frontendFolder, filename), { encoding: "utf-8" }).replace(
          /\r\n/g,
          "\n"
        );
        const hash = createHash("sha256").update(fileBuffer, "utf8").digest("hex");
        const fileKey = line.substring(line.indexOf("jar-resources/") + 14);
        frontendFiles[fileKey] = hash;
      });
      let frontendFolderAlias = "Frontend";
      generatedImports.filter((line) => line.startsWith(frontendFolderAlias + "/")).filter((line) => !line.startsWith(frontendFolderAlias + "/generated/")).filter((line) => !line.startsWith(frontendFolderAlias + "/themes/")).map((line) => line.substring(frontendFolderAlias.length + 1)).filter((line) => !frontendFiles[line]).forEach((line) => {
        const filePath = path.resolve(frontendFolder, line);
        if (projectFileExtensions.includes(path.extname(filePath)) && existsSync5(filePath)) {
          const fileBuffer = readFileSync4(filePath, { encoding: "utf-8" }).replace(/\r\n/g, "\n");
          frontendFiles[line] = createHash("sha256").update(fileBuffer, "utf8").digest("hex");
        }
      });
      if (existsSync5(path.resolve(frontendFolder, "index.ts"))) {
        const fileBuffer = readFileSync4(path.resolve(frontendFolder, "index.ts"), { encoding: "utf-8" }).replace(
          /\r\n/g,
          "\n"
        );
        frontendFiles[`index.ts`] = createHash("sha256").update(fileBuffer, "utf8").digest("hex");
      }
      const themeJsonContents = {};
      const themesFolder = path.resolve(jarResourcesFolder, "themes");
      if (existsSync5(themesFolder)) {
        readdirSync2(themesFolder).forEach((themeFolder2) => {
          const themeJson = path.resolve(themesFolder, themeFolder2, "theme.json");
          if (existsSync5(themeJson)) {
            themeJsonContents[path.basename(themeFolder2)] = readFileSync4(themeJson, { encoding: "utf-8" }).replace(
              /\r\n/g,
              "\n"
            );
          }
        });
      }
      collectThemeJsonsInFrontend(themeJsonContents, vaadin_dev_server_settings_default.themeName);
      let webComponents = [];
      if (webComponentTags) {
        webComponents = webComponentTags.split(";");
      }
      const stats = {
        packageJsonDependencies: projectPackageJson.dependencies,
        npmModules: npmModuleAndVersion,
        bundleImports: generatedImports,
        frontendHashes: frontendFiles,
        themeJsonContents,
        entryScripts,
        webComponents,
        cvdlModules: cvdls,
        packageJsonHash: projectPackageJson?.vaadin?.hash,
        indexHtmlGenerated: rowsGenerated
      };
      writeFileSync2(statsFile, JSON.stringify(stats, null, 1));
    }
  };
}
function vaadinBundlesPlugin() {
  const disabledMessage = "Vaadin component dependency bundles are disabled.";
  const modulesDirectory = nodeModulesFolder.replace(/\\/g, "/");
  let vaadinBundleJson;
  function parseModuleId(id) {
    const [scope, scopedPackageName] = id.split("/", 3);
    const packageName = scope.startsWith("@") ? `${scope}/${scopedPackageName}` : scope;
    const modulePath = `.${id.substring(packageName.length)}`;
    return {
      packageName,
      modulePath
    };
  }
  function getExports(id) {
    const { packageName, modulePath } = parseModuleId(id);
    const packageInfo = vaadinBundleJson.packages[packageName];
    if (!packageInfo)
      return;
    const exposeInfo = packageInfo.exposes[modulePath];
    if (!exposeInfo)
      return;
    const exportsSet = /* @__PURE__ */ new Set();
    for (const e of exposeInfo.exports) {
      if (typeof e === "string") {
        exportsSet.add(e);
      } else {
        const { namespace, source } = e;
        if (namespace) {
          exportsSet.add(namespace);
        } else {
          const sourceExports = getExports(source);
          if (sourceExports) {
            sourceExports.forEach((e2) => exportsSet.add(e2));
          }
        }
      }
    }
    return Array.from(exportsSet);
  }
  function getExportBinding(binding) {
    return binding === "default" ? "_default as default" : binding;
  }
  function getImportAssigment(binding) {
    return binding === "default" ? "default: _default" : binding;
  }
  return {
    name: "vaadin:bundles",
    enforce: "pre",
    apply(config, { command }) {
      if (command !== "serve")
        return false;
      try {
        const vaadinBundleJsonPath = require2.resolve("@vaadin/bundles/vaadin-bundle.json");
        vaadinBundleJson = JSON.parse(readFileSync4(vaadinBundleJsonPath, { encoding: "utf8" }));
      } catch (e) {
        if (typeof e === "object" && e.code === "MODULE_NOT_FOUND") {
          vaadinBundleJson = { packages: {} };
          console.info(`@vaadin/bundles npm package is not found, ${disabledMessage}`);
          return false;
        } else {
          throw e;
        }
      }
      const versionMismatches = [];
      for (const [name, packageInfo] of Object.entries(vaadinBundleJson.packages)) {
        let installedVersion = void 0;
        try {
          const { version: bundledVersion } = packageInfo;
          const installedPackageJsonFile = path.resolve(modulesDirectory, name, "package.json");
          const packageJson = JSON.parse(readFileSync4(installedPackageJsonFile, { encoding: "utf8" }));
          installedVersion = packageJson.version;
          if (installedVersion && installedVersion !== bundledVersion) {
            versionMismatches.push({
              name,
              bundledVersion,
              installedVersion
            });
          }
        } catch (_) {
        }
      }
      if (versionMismatches.length) {
        console.info(`@vaadin/bundles has version mismatches with installed packages, ${disabledMessage}`);
        console.info(`Packages with version mismatches: ${JSON.stringify(versionMismatches, void 0, 2)}`);
        vaadinBundleJson = { packages: {} };
        return false;
      }
      return true;
    },
    async config(config) {
      return mergeConfig(
        {
          optimizeDeps: {
            exclude: [
              // Vaadin bundle
              "@vaadin/bundles",
              ...Object.keys(vaadinBundleJson.packages),
              "@vaadin/vaadin-material-styles"
            ]
          }
        },
        config
      );
    },
    load(rawId) {
      const [path2, params] = rawId.split("?");
      if (!path2.startsWith(modulesDirectory))
        return;
      const id = path2.substring(modulesDirectory.length + 1);
      const bindings = getExports(id);
      if (bindings === void 0)
        return;
      const cacheSuffix = params ? `?${params}` : "";
      const bundlePath = `@vaadin/bundles/vaadin.js${cacheSuffix}`;
      return `import { init as VaadinBundleInit, get as VaadinBundleGet } from '${bundlePath}';
await VaadinBundleInit('default');
const { ${bindings.map(getImportAssigment).join(", ")} } = (await VaadinBundleGet('./node_modules/${id}'))();
export { ${bindings.map(getExportBinding).join(", ")} };`;
    }
  };
}
function themePlugin(opts) {
  const fullThemeOptions = { ...themeOptions, devMode: opts.devMode };
  return {
    name: "vaadin:theme",
    config() {
      processThemeResources(fullThemeOptions, console);
    },
    configureServer(server) {
      function handleThemeFileCreateDelete(themeFile, stats) {
        if (themeFile.startsWith(themeFolder)) {
          const changed = path.relative(themeFolder, themeFile);
          console.debug("Theme file " + (!!stats ? "created" : "deleted"), changed);
          processThemeResources(fullThemeOptions, console);
        }
      }
      server.watcher.on("add", handleThemeFileCreateDelete);
      server.watcher.on("unlink", handleThemeFileCreateDelete);
    },
    handleHotUpdate(context) {
      const contextPath = path.resolve(context.file);
      const themePath = path.resolve(themeFolder);
      if (contextPath.startsWith(themePath)) {
        const changed = path.relative(themePath, contextPath);
        console.debug("Theme file changed", changed);
        if (changed.startsWith(vaadin_dev_server_settings_default.themeName)) {
          processThemeResources(fullThemeOptions, console);
        }
      }
    },
    async resolveId(id, importer) {
      if (path.resolve(themeOptions.frontendGeneratedFolder, "theme.js") === importer && !existsSync5(path.resolve(themeOptions.frontendGeneratedFolder, id))) {
        console.debug("Generate theme file " + id + " not existing. Processing theme resource");
        processThemeResources(fullThemeOptions, console);
        return;
      }
      if (!id.startsWith(vaadin_dev_server_settings_default.themeFolder)) {
        return;
      }
      for (const location of [themeResourceFolder, frontendFolder]) {
        const result = await this.resolve(path.resolve(location, id));
        if (result) {
          return result;
        }
      }
    },
    async transform(raw, id, options) {
      const [bareId, query] = id.split("?");
      if (!bareId?.startsWith(themeFolder) && !bareId?.startsWith(themeOptions.themeResourceFolder) || !bareId?.endsWith(".css")) {
        return;
      }
      const resourceThemeFolder = bareId.startsWith(themeFolder) ? themeFolder : themeOptions.themeResourceFolder;
      const [themeName] = bareId.substring(resourceThemeFolder.length + 1).split("/");
      return rewriteCssUrls(raw, path.dirname(bareId), path.resolve(resourceThemeFolder, themeName), console, opts);
    }
  };
}
function runWatchDog(watchDogPort, watchDogHost) {
  const client = net.Socket();
  client.setEncoding("utf8");
  client.on("error", function(err) {
    console.log("Watchdog connection error. Terminating vite process...", err);
    client.destroy();
    process.exit(0);
  });
  client.on("close", function() {
    client.destroy();
    runWatchDog(watchDogPort, watchDogHost);
  });
  client.connect(watchDogPort, watchDogHost || "localhost");
}
var allowedFrontendFolders = [frontendFolder, nodeModulesFolder];
function showRecompileReason() {
  return {
    name: "vaadin:why-you-compile",
    handleHotUpdate(context) {
      console.log("Recompiling because", context.file, "changed");
    }
  };
}
var DEV_MODE_START_REGEXP = /\/\*[\*!]\s+vaadin-dev-mode:start/;
var DEV_MODE_CODE_REGEXP = /\/\*[\*!]\s+vaadin-dev-mode:start([\s\S]*)vaadin-dev-mode:end\s+\*\*\//i;
function preserveUsageStats() {
  return {
    name: "vaadin:preserve-usage-stats",
    transform(src, id) {
      if (id.includes("vaadin-usage-statistics")) {
        if (src.includes("vaadin-dev-mode:start")) {
          const newSrc = src.replace(DEV_MODE_START_REGEXP, "/*! vaadin-dev-mode:start");
          if (newSrc === src) {
            console.error("Comment replacement failed to change anything");
          } else if (!newSrc.match(DEV_MODE_CODE_REGEXP)) {
            console.error("New comment fails to match original regexp");
          } else {
            return { code: newSrc };
          }
        }
      }
      return { code: src };
    }
  };
}
var vaadinConfig = (env) => {
  const devMode = env.mode === "development";
  const productionMode = !devMode && !devBundle;
  if (devMode && process.env.watchDogPort) {
    runWatchDog(process.env.watchDogPort, process.env.watchDogHost);
  }
  return {
    root: frontendFolder,
    base: "",
    publicDir: false,
    resolve: {
      alias: {
        "@vaadin/flow-frontend": jarResourcesFolder,
        Frontend: frontendFolder
      },
      preserveSymlinks: true
    },
    define: {
      OFFLINE_PATH: vaadin_dev_server_settings_default.offlinePath,
      VITE_ENABLED: "true"
    },
    server: {
      host: "127.0.0.1",
      strictPort: true,
      fs: {
        allow: allowedFrontendFolders
      }
    },
    build: {
      minify: productionMode,
      outDir: buildOutputFolder,
      emptyOutDir: devBundle,
      assetsDir: "VAADIN/build",
      target: ["esnext", "safari15"],
      rollupOptions: {
        input: {
          indexhtml: projectIndexHtml,
          ...hasExportedWebComponents ? { webcomponenthtml: path.resolve(frontendFolder, "web-component.html") } : {}
        },
        onwarn: (warning, defaultHandler) => {
          const ignoreEvalWarning = [
            "generated/jar-resources/FlowClient.js",
            "generated/jar-resources/vaadin-spreadsheet/spreadsheet-export.js",
            "@vaadin/charts/src/helpers.js"
          ];
          if (warning.code === "EVAL" && warning.id && !!ignoreEvalWarning.find((id) => warning.id.endsWith(id))) {
            return;
          }
          defaultHandler(warning);
        }
      }
    },
    optimizeDeps: {
      entries: [
        // Pre-scan entrypoints in Vite to avoid reloading on first open
        "generated/vaadin.ts"
      ],
      exclude: [
        "@vaadin/router",
        "@vaadin/vaadin-license-checker",
        "@vaadin/vaadin-usage-statistics",
        "workbox-core",
        "workbox-precaching",
        "workbox-routing",
        "workbox-strategies"
      ]
    },
    plugins: [
      productionMode && brotli(),
      devMode && vaadinBundlesPlugin(),
      devMode && showRecompileReason(),
      vaadin_dev_server_settings_default.offlineEnabled && buildSWPlugin({ devMode }),
      !devMode && statsExtracterPlugin(),
      devBundle && preserveUsageStats(),
      themePlugin({ devMode }),
      postcssLit({
        include: ["**/*.css", /.*\/.*\.css\?.*/],
        exclude: [
          `${themeFolder}/**/*.css`,
          new RegExp(`${themeFolder}/.*/.*\\.css\\?.*`),
          `${themeResourceFolder}/**/*.css`,
          new RegExp(`${themeResourceFolder}/.*/.*\\.css\\?.*`),
          new RegExp(".*/.*\\?html-proxy.*")
        ]
      }),
      // The React plugin provides fast refresh and debug source info
      reactPlugin({
        include: "**/*.tsx",
        babel: {
          // We need to use babel to provide the source information for it to be correct
          // (otherwise Babel will slightly rewrite the source file and esbuild generate source info for the modified file)
          presets: [["@babel/preset-react", { runtime: "automatic", development: !productionMode }]],
          // React writes the source location for where components are used, this writes for where they are defined
          plugins: [
            !productionMode && addFunctionComponentSourceLocationBabel()
          ].filter(Boolean)
        }
      }),
      {
        name: "vaadin:force-remove-html-middleware",
        configureServer(server) {
          return () => {
            server.middlewares.stack = server.middlewares.stack.filter((mw) => {
              const handleName = `${mw.handle}`;
              return !handleName.includes("viteHtmlFallbackMiddleware");
            });
          };
        }
      },
      hasExportedWebComponents && {
        name: "vaadin:inject-entrypoints-to-web-component-html",
        transformIndexHtml: {
          order: "pre",
          handler(_html, { path: path2, server }) {
            if (path2 !== "/web-component.html") {
              return;
            }
            return [
              {
                tag: "script",
                attrs: { type: "module", src: `/generated/vaadin-web-component.ts` },
                injectTo: "head"
              }
            ];
          }
        }
      },
      {
        name: "vaadin:inject-entrypoints-to-index-html",
        transformIndexHtml: {
          order: "pre",
          handler(_html, { path: path2, server }) {
            if (path2 !== "/index.html") {
              return;
            }
            const scripts = [];
            if (devMode) {
              scripts.push({
                tag: "script",
                attrs: { type: "module", src: `/generated/vite-devmode.ts` },
                injectTo: "head"
              });
            }
            scripts.push({
              tag: "script",
              attrs: { type: "module", src: "/generated/vaadin.ts" },
              injectTo: "head"
            });
            return scripts;
          }
        }
      },
      checker({
        typescript: true
      }),
      productionMode && visualizer({ brotliSize: true, filename: bundleSizeFile })
    ]
  };
};
var overrideVaadinConfig = (customConfig2) => {
  return defineConfig((env) => mergeConfig(vaadinConfig(env), customConfig2(env)));
};
function getVersion(module) {
  const packageJson = path.resolve(nodeModulesFolder, module, "package.json");
  return JSON.parse(readFileSync4(packageJson, { encoding: "utf-8" })).version;
}
function getCvdlName(module) {
  const packageJson = path.resolve(nodeModulesFolder, module, "package.json");
  return JSON.parse(readFileSync4(packageJson, { encoding: "utf-8" })).cvdlName;
}

// vite.config.ts
var customConfig = (env) => ({
  // Here you can add custom Vite parameters
  // https://vitejs.dev/config/
});
var vite_config_default = overrideVaadinConfig(customConfig);
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5nZW5lcmF0ZWQudHMiLCAidGFyZ2V0L3BsdWdpbnMvYXBwbGljYXRpb24tdGhlbWUtcGx1Z2luL3RoZW1lLWhhbmRsZS5qcyIsICJ0YXJnZXQvcGx1Z2lucy9hcHBsaWNhdGlvbi10aGVtZS1wbHVnaW4vdGhlbWUtZ2VuZXJhdG9yLmpzIiwgInRhcmdldC9wbHVnaW5zL2FwcGxpY2F0aW9uLXRoZW1lLXBsdWdpbi90aGVtZS1jb3B5LmpzIiwgInRhcmdldC9wbHVnaW5zL3RoZW1lLWxvYWRlci90aGVtZS1sb2FkZXItdXRpbHMuanMiLCAidGFyZ2V0L3BsdWdpbnMvcmVhY3QtZnVuY3Rpb24tbG9jYXRpb24tcGx1Z2luL3JlYWN0LWZ1bmN0aW9uLWxvY2F0aW9uLXBsdWdpbi5qcyIsICJ0YXJnZXQvdmFhZGluLWRldi1zZXJ2ZXItc2V0dGluZ3MuanNvbiIsICJ0YXJnZXQvcGx1Z2lucy9yb2xsdXAtcGx1Z2luLXBvc3Rjc3MtbGl0LWN1c3RvbS9yb2xsdXAtcGx1Z2luLXBvc3Rjc3MtbGl0LmpzIiwgInZpdGUuY29uZmlnLnRzIl0sCiAgInNvdXJjZXNDb250ZW50IjogWyJjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfZGlybmFtZSA9IFwiRjpcXFxcUHJveWVjdG8tU2VndXJpZGFkXFxcXGdlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWFcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfZmlsZW5hbWUgPSBcIkY6XFxcXFByb3llY3RvLVNlZ3VyaWRhZFxcXFxnZXN0aW9uLXNlZ3VyaWRhZC1hZXJvcG9ydHVhcmlhXFxcXHZpdGUuZ2VuZXJhdGVkLnRzXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ltcG9ydF9tZXRhX3VybCA9IFwiZmlsZTovLy9GOi9Qcm95ZWN0by1TZWd1cmlkYWQvZ2VzdGlvbi1zZWd1cmlkYWQtYWVyb3BvcnR1YXJpYS92aXRlLmdlbmVyYXRlZC50c1wiOy8qKlxuICogTk9USUNFOiB0aGlzIGlzIGFuIGF1dG8tZ2VuZXJhdGVkIGZpbGVcbiAqXG4gKiBUaGlzIGZpbGUgaGFzIGJlZW4gZ2VuZXJhdGVkIGJ5IHRoZSBgZmxvdzpwcmVwYXJlLWZyb250ZW5kYCBtYXZlbiBnb2FsLlxuICogVGhpcyBmaWxlIHdpbGwgYmUgb3ZlcndyaXR0ZW4gb24gZXZlcnkgcnVuLiBBbnkgY3VzdG9tIGNoYW5nZXMgc2hvdWxkIGJlIG1hZGUgdG8gdml0ZS5jb25maWcudHNcbiAqL1xuaW1wb3J0IHBhdGggZnJvbSAncGF0aCc7XG5pbXBvcnQgeyBleGlzdHNTeW5jLCBta2RpclN5bmMsIHJlYWRkaXJTeW5jLCByZWFkRmlsZVN5bmMsIHdyaXRlRmlsZVN5bmMgfSBmcm9tICdmcyc7XG5pbXBvcnQgeyBjcmVhdGVIYXNoIH0gZnJvbSAnY3J5cHRvJztcbmltcG9ydCAqIGFzIG5ldCBmcm9tICduZXQnO1xuXG5pbXBvcnQgeyBwcm9jZXNzVGhlbWVSZXNvdXJjZXMgfSBmcm9tICcuL3RhcmdldC9wbHVnaW5zL2FwcGxpY2F0aW9uLXRoZW1lLXBsdWdpbi90aGVtZS1oYW5kbGUuanMnO1xuaW1wb3J0IHsgcmV3cml0ZUNzc1VybHMgfSBmcm9tICcuL3RhcmdldC9wbHVnaW5zL3RoZW1lLWxvYWRlci90aGVtZS1sb2FkZXItdXRpbHMuanMnO1xuaW1wb3J0IHsgYWRkRnVuY3Rpb25Db21wb25lbnRTb3VyY2VMb2NhdGlvbkJhYmVsIH0gZnJvbSAnLi90YXJnZXQvcGx1Z2lucy9yZWFjdC1mdW5jdGlvbi1sb2NhdGlvbi1wbHVnaW4vcmVhY3QtZnVuY3Rpb24tbG9jYXRpb24tcGx1Z2luLmpzJztcbmltcG9ydCBzZXR0aW5ncyBmcm9tICcuL3RhcmdldC92YWFkaW4tZGV2LXNlcnZlci1zZXR0aW5ncy5qc29uJztcbmltcG9ydCB7XG4gIEFzc2V0SW5mbyxcbiAgQ2h1bmtJbmZvLFxuICBkZWZpbmVDb25maWcsXG4gIG1lcmdlQ29uZmlnLFxuICBPdXRwdXRPcHRpb25zLFxuICBQbHVnaW5PcHRpb24sXG4gIFJlc29sdmVkQ29uZmlnLFxuICBVc2VyQ29uZmlnRm5cbn0gZnJvbSAndml0ZSc7XG5pbXBvcnQgeyBnZXRNYW5pZmVzdCB9IGZyb20gJ3dvcmtib3gtYnVpbGQnO1xuXG5pbXBvcnQgKiBhcyByb2xsdXAgZnJvbSAncm9sbHVwJztcbmltcG9ydCBicm90bGkgZnJvbSAncm9sbHVwLXBsdWdpbi1icm90bGknO1xuaW1wb3J0IHJlcGxhY2UgZnJvbSAnQHJvbGx1cC9wbHVnaW4tcmVwbGFjZSc7XG5pbXBvcnQgY2hlY2tlciBmcm9tICd2aXRlLXBsdWdpbi1jaGVja2VyJztcbmltcG9ydCBwb3N0Y3NzTGl0IGZyb20gJy4vdGFyZ2V0L3BsdWdpbnMvcm9sbHVwLXBsdWdpbi1wb3N0Y3NzLWxpdC1jdXN0b20vcm9sbHVwLXBsdWdpbi1wb3N0Y3NzLWxpdC5qcyc7XG5cbmltcG9ydCB7IGNyZWF0ZVJlcXVpcmUgfSBmcm9tICdtb2R1bGUnO1xuXG5pbXBvcnQgeyB2aXN1YWxpemVyIH0gZnJvbSAncm9sbHVwLXBsdWdpbi12aXN1YWxpemVyJztcbmltcG9ydCByZWFjdFBsdWdpbiBmcm9tICdAdml0ZWpzL3BsdWdpbi1yZWFjdCc7XG5cblxuXG4vLyBNYWtlIGByZXF1aXJlYCBjb21wYXRpYmxlIHdpdGggRVMgbW9kdWxlc1xuY29uc3QgcmVxdWlyZSA9IGNyZWF0ZVJlcXVpcmUoaW1wb3J0Lm1ldGEudXJsKTtcblxuY29uc3QgYXBwU2hlbGxVcmwgPSAnLic7XG5cbmNvbnN0IGZyb250ZW5kRm9sZGVyID0gcGF0aC5yZXNvbHZlKF9fZGlybmFtZSwgc2V0dGluZ3MuZnJvbnRlbmRGb2xkZXIpO1xuY29uc3QgdGhlbWVGb2xkZXIgPSBwYXRoLnJlc29sdmUoZnJvbnRlbmRGb2xkZXIsIHNldHRpbmdzLnRoZW1lRm9sZGVyKTtcbmNvbnN0IGZyb250ZW5kQnVuZGxlRm9sZGVyID0gcGF0aC5yZXNvbHZlKF9fZGlybmFtZSwgc2V0dGluZ3MuZnJvbnRlbmRCdW5kbGVPdXRwdXQpO1xuY29uc3QgZGV2QnVuZGxlRm9sZGVyID0gcGF0aC5yZXNvbHZlKF9fZGlybmFtZSwgc2V0dGluZ3MuZGV2QnVuZGxlT3V0cHV0KTtcbmNvbnN0IGRldkJ1bmRsZSA9ICEhcHJvY2Vzcy5lbnYuZGV2QnVuZGxlO1xuY29uc3QgamFyUmVzb3VyY2VzRm9sZGVyID0gcGF0aC5yZXNvbHZlKF9fZGlybmFtZSwgc2V0dGluZ3MuamFyUmVzb3VyY2VzRm9sZGVyKTtcbmNvbnN0IHRoZW1lUmVzb3VyY2VGb2xkZXIgPSBwYXRoLnJlc29sdmUoX19kaXJuYW1lLCBzZXR0aW5ncy50aGVtZVJlc291cmNlRm9sZGVyKTtcbmNvbnN0IHByb2plY3RQYWNrYWdlSnNvbkZpbGUgPSBwYXRoLnJlc29sdmUoX19kaXJuYW1lLCAncGFja2FnZS5qc29uJyk7XG5cbmNvbnN0IGJ1aWxkT3V0cHV0Rm9sZGVyID0gZGV2QnVuZGxlID8gZGV2QnVuZGxlRm9sZGVyIDogZnJvbnRlbmRCdW5kbGVGb2xkZXI7XG5jb25zdCBzdGF0c0ZvbGRlciA9IHBhdGgucmVzb2x2ZShfX2Rpcm5hbWUsIGRldkJ1bmRsZSA/IHNldHRpbmdzLmRldkJ1bmRsZVN0YXRzT3V0cHV0IDogc2V0dGluZ3Muc3RhdHNPdXRwdXQpO1xuY29uc3Qgc3RhdHNGaWxlID0gcGF0aC5yZXNvbHZlKHN0YXRzRm9sZGVyLCAnc3RhdHMuanNvbicpO1xuY29uc3QgYnVuZGxlU2l6ZUZpbGUgPSBwYXRoLnJlc29sdmUoc3RhdHNGb2xkZXIsICdidW5kbGUtc2l6ZS5odG1sJyk7XG5jb25zdCBub2RlTW9kdWxlc0ZvbGRlciA9IHBhdGgucmVzb2x2ZShfX2Rpcm5hbWUsICdub2RlX21vZHVsZXMnKTtcbmNvbnN0IHdlYkNvbXBvbmVudFRhZ3MgPSAnJztcblxuY29uc3QgcHJvamVjdEluZGV4SHRtbCA9IHBhdGgucmVzb2x2ZShmcm9udGVuZEZvbGRlciwgJ2luZGV4Lmh0bWwnKTtcblxuY29uc3QgcHJvamVjdFN0YXRpY0Fzc2V0c0ZvbGRlcnMgPSBbXG4gIHBhdGgucmVzb2x2ZShfX2Rpcm5hbWUsICdzcmMnLCAnbWFpbicsICdyZXNvdXJjZXMnLCAnTUVUQS1JTkYnLCAncmVzb3VyY2VzJyksXG4gIHBhdGgucmVzb2x2ZShfX2Rpcm5hbWUsICdzcmMnLCAnbWFpbicsICdyZXNvdXJjZXMnLCAnc3RhdGljJyksXG4gIGZyb250ZW5kRm9sZGVyXG5dO1xuXG4vLyBGb2xkZXJzIGluIHRoZSBwcm9qZWN0IHdoaWNoIGNhbiBjb250YWluIGFwcGxpY2F0aW9uIHRoZW1lc1xuY29uc3QgdGhlbWVQcm9qZWN0Rm9sZGVycyA9IHByb2plY3RTdGF0aWNBc3NldHNGb2xkZXJzLm1hcCgoZm9sZGVyKSA9PiBwYXRoLnJlc29sdmUoZm9sZGVyLCBzZXR0aW5ncy50aGVtZUZvbGRlcikpO1xuXG5jb25zdCB0aGVtZU9wdGlvbnMgPSB7XG4gIGRldk1vZGU6IGZhbHNlLFxuICB1c2VEZXZCdW5kbGU6IGRldkJ1bmRsZSxcbiAgLy8gVGhlIGZvbGxvd2luZyBtYXRjaGVzIGZvbGRlciAnZnJvbnRlbmQvZ2VuZXJhdGVkL3RoZW1lcy8nXG4gIC8vIChub3QgJ2Zyb250ZW5kL3RoZW1lcycpIGZvciB0aGVtZSBpbiBKQVIgdGhhdCBpcyBjb3BpZWQgdGhlcmVcbiAgdGhlbWVSZXNvdXJjZUZvbGRlcjogcGF0aC5yZXNvbHZlKHRoZW1lUmVzb3VyY2VGb2xkZXIsIHNldHRpbmdzLnRoZW1lRm9sZGVyKSxcbiAgdGhlbWVQcm9qZWN0Rm9sZGVyczogdGhlbWVQcm9qZWN0Rm9sZGVycyxcbiAgcHJvamVjdFN0YXRpY0Fzc2V0c091dHB1dEZvbGRlcjogZGV2QnVuZGxlXG4gICAgPyBwYXRoLnJlc29sdmUoZGV2QnVuZGxlRm9sZGVyLCAnLi4vYXNzZXRzJylcbiAgICA6IHBhdGgucmVzb2x2ZShfX2Rpcm5hbWUsIHNldHRpbmdzLnN0YXRpY091dHB1dCksXG4gIGZyb250ZW5kR2VuZXJhdGVkRm9sZGVyOiBwYXRoLnJlc29sdmUoZnJvbnRlbmRGb2xkZXIsIHNldHRpbmdzLmdlbmVyYXRlZEZvbGRlcilcbn07XG5cbmNvbnN0IGhhc0V4cG9ydGVkV2ViQ29tcG9uZW50cyA9IGV4aXN0c1N5bmMocGF0aC5yZXNvbHZlKGZyb250ZW5kRm9sZGVyLCAnd2ViLWNvbXBvbmVudC5odG1sJykpO1xuXG4vLyBCbG9jayBkZWJ1ZyBhbmQgdHJhY2UgbG9ncy5cbmNvbnNvbGUudHJhY2UgPSAoKSA9PiB7fTtcbmNvbnNvbGUuZGVidWcgPSAoKSA9PiB7fTtcblxuZnVuY3Rpb24gaW5qZWN0TWFuaWZlc3RUb1NXUGx1Z2luKCk6IHJvbGx1cC5QbHVnaW4ge1xuICBjb25zdCByZXdyaXRlTWFuaWZlc3RJbmRleEh0bWxVcmwgPSAobWFuaWZlc3QpID0+IHtcbiAgICBjb25zdCBpbmRleEVudHJ5ID0gbWFuaWZlc3QuZmluZCgoZW50cnkpID0+IGVudHJ5LnVybCA9PT0gJ2luZGV4Lmh0bWwnKTtcbiAgICBpZiAoaW5kZXhFbnRyeSkge1xuICAgICAgaW5kZXhFbnRyeS51cmwgPSBhcHBTaGVsbFVybDtcbiAgICB9XG5cbiAgICByZXR1cm4geyBtYW5pZmVzdCwgd2FybmluZ3M6IFtdIH07XG4gIH07XG5cbiAgcmV0dXJuIHtcbiAgICBuYW1lOiAndmFhZGluOmluamVjdC1tYW5pZmVzdC10by1zdycsXG4gICAgYXN5bmMgdHJhbnNmb3JtKGNvZGUsIGlkKSB7XG4gICAgICBpZiAoL3N3XFwuKHRzfGpzKSQvLnRlc3QoaWQpKSB7XG4gICAgICAgIGNvbnN0IHsgbWFuaWZlc3RFbnRyaWVzIH0gPSBhd2FpdCBnZXRNYW5pZmVzdCh7XG4gICAgICAgICAgZ2xvYkRpcmVjdG9yeTogYnVpbGRPdXRwdXRGb2xkZXIsXG4gICAgICAgICAgZ2xvYlBhdHRlcm5zOiBbJyoqLyonXSxcbiAgICAgICAgICBnbG9iSWdub3JlczogWycqKi8qLmJyJ10sXG4gICAgICAgICAgbWFuaWZlc3RUcmFuc2Zvcm1zOiBbcmV3cml0ZU1hbmlmZXN0SW5kZXhIdG1sVXJsXSxcbiAgICAgICAgICBtYXhpbXVtRmlsZVNpemVUb0NhY2hlSW5CeXRlczogMTAwICogMTAyNCAqIDEwMjQgLy8gMTAwbWIsXG4gICAgICAgIH0pO1xuXG4gICAgICAgIHJldHVybiBjb2RlLnJlcGxhY2UoJ3NlbGYuX19XQl9NQU5JRkVTVCcsIEpTT04uc3RyaW5naWZ5KG1hbmlmZXN0RW50cmllcykpO1xuICAgICAgfVxuICAgIH1cbiAgfTtcbn1cblxuZnVuY3Rpb24gYnVpbGRTV1BsdWdpbihvcHRzKTogUGx1Z2luT3B0aW9uIHtcbiAgbGV0IGNvbmZpZzogUmVzb2x2ZWRDb25maWc7XG4gIGNvbnN0IGRldk1vZGUgPSBvcHRzLmRldk1vZGU7XG5cbiAgY29uc3Qgc3dPYmogPSB7fTtcblxuICBhc3luYyBmdW5jdGlvbiBidWlsZChhY3Rpb246ICdnZW5lcmF0ZScgfCAnd3JpdGUnLCBhZGRpdGlvbmFsUGx1Z2luczogcm9sbHVwLlBsdWdpbltdID0gW10pIHtcbiAgICBjb25zdCBpbmNsdWRlZFBsdWdpbk5hbWVzID0gW1xuICAgICAgJ3ZpdGU6ZXNidWlsZCcsXG4gICAgICAncm9sbHVwLXBsdWdpbi1keW5hbWljLWltcG9ydC12YXJpYWJsZXMnLFxuICAgICAgJ3ZpdGU6ZXNidWlsZC10cmFuc3BpbGUnLFxuICAgICAgJ3ZpdGU6dGVyc2VyJ1xuICAgIF07XG4gICAgY29uc3QgcGx1Z2luczogcm9sbHVwLlBsdWdpbltdID0gY29uZmlnLnBsdWdpbnMuZmlsdGVyKChwKSA9PiB7XG4gICAgICByZXR1cm4gaW5jbHVkZWRQbHVnaW5OYW1lcy5pbmNsdWRlcyhwLm5hbWUpO1xuICAgIH0pO1xuICAgIGNvbnN0IHJlc29sdmVyID0gY29uZmlnLmNyZWF0ZVJlc29sdmVyKCk7XG4gICAgY29uc3QgcmVzb2x2ZVBsdWdpbjogcm9sbHVwLlBsdWdpbiA9IHtcbiAgICAgIG5hbWU6ICdyZXNvbHZlcicsXG4gICAgICByZXNvbHZlSWQoc291cmNlLCBpbXBvcnRlciwgX29wdGlvbnMpIHtcbiAgICAgICAgcmV0dXJuIHJlc29sdmVyKHNvdXJjZSwgaW1wb3J0ZXIpO1xuICAgICAgfVxuICAgIH07XG4gICAgcGx1Z2lucy51bnNoaWZ0KHJlc29sdmVQbHVnaW4pOyAvLyBQdXQgcmVzb2x2ZSBmaXJzdFxuICAgIHBsdWdpbnMucHVzaChcbiAgICAgIHJlcGxhY2Uoe1xuICAgICAgICB2YWx1ZXM6IHtcbiAgICAgICAgICAncHJvY2Vzcy5lbnYuTk9ERV9FTlYnOiBKU09OLnN0cmluZ2lmeShjb25maWcubW9kZSksXG4gICAgICAgICAgLi4uY29uZmlnLmRlZmluZVxuICAgICAgICB9LFxuICAgICAgICBwcmV2ZW50QXNzaWdubWVudDogdHJ1ZVxuICAgICAgfSlcbiAgICApO1xuICAgIGlmIChhZGRpdGlvbmFsUGx1Z2lucykge1xuICAgICAgcGx1Z2lucy5wdXNoKC4uLmFkZGl0aW9uYWxQbHVnaW5zKTtcbiAgICB9XG4gICAgY29uc3QgYnVuZGxlID0gYXdhaXQgcm9sbHVwLnJvbGx1cCh7XG4gICAgICBpbnB1dDogcGF0aC5yZXNvbHZlKHNldHRpbmdzLmNsaWVudFNlcnZpY2VXb3JrZXJTb3VyY2UpLFxuICAgICAgcGx1Z2luc1xuICAgIH0pO1xuXG4gICAgdHJ5IHtcbiAgICAgIHJldHVybiBhd2FpdCBidW5kbGVbYWN0aW9uXSh7XG4gICAgICAgIGZpbGU6IHBhdGgucmVzb2x2ZShidWlsZE91dHB1dEZvbGRlciwgJ3N3LmpzJyksXG4gICAgICAgIGZvcm1hdDogJ2VzJyxcbiAgICAgICAgZXhwb3J0czogJ25vbmUnLFxuICAgICAgICBzb3VyY2VtYXA6IGNvbmZpZy5jb21tYW5kID09PSAnc2VydmUnIHx8IGNvbmZpZy5idWlsZC5zb3VyY2VtYXAsXG4gICAgICAgIGlubGluZUR5bmFtaWNJbXBvcnRzOiB0cnVlXG4gICAgICB9KTtcbiAgICB9IGZpbmFsbHkge1xuICAgICAgYXdhaXQgYnVuZGxlLmNsb3NlKCk7XG4gICAgfVxuICB9XG5cbiAgcmV0dXJuIHtcbiAgICBuYW1lOiAndmFhZGluOmJ1aWxkLXN3JyxcbiAgICBlbmZvcmNlOiAncG9zdCcsXG4gICAgYXN5bmMgY29uZmlnUmVzb2x2ZWQocmVzb2x2ZWRDb25maWcpIHtcbiAgICAgIGNvbmZpZyA9IHJlc29sdmVkQ29uZmlnO1xuICAgIH0sXG4gICAgYXN5bmMgYnVpbGRTdGFydCgpIHtcbiAgICAgIGlmIChkZXZNb2RlKSB7XG4gICAgICAgIGNvbnN0IHsgb3V0cHV0IH0gPSBhd2FpdCBidWlsZCgnZ2VuZXJhdGUnKTtcbiAgICAgICAgc3dPYmouY29kZSA9IG91dHB1dFswXS5jb2RlO1xuICAgICAgICBzd09iai5tYXAgPSBvdXRwdXRbMF0ubWFwO1xuICAgICAgfVxuICAgIH0sXG4gICAgYXN5bmMgbG9hZChpZCkge1xuICAgICAgaWYgKGlkLmVuZHNXaXRoKCdzdy5qcycpKSB7XG4gICAgICAgIHJldHVybiAnJztcbiAgICAgIH1cbiAgICB9LFxuICAgIGFzeW5jIHRyYW5zZm9ybShfY29kZSwgaWQpIHtcbiAgICAgIGlmIChpZC5lbmRzV2l0aCgnc3cuanMnKSkge1xuICAgICAgICByZXR1cm4gc3dPYmo7XG4gICAgICB9XG4gICAgfSxcbiAgICBhc3luYyBjbG9zZUJ1bmRsZSgpIHtcbiAgICAgIGlmICghZGV2TW9kZSkge1xuICAgICAgICBhd2FpdCBidWlsZCgnd3JpdGUnLCBbaW5qZWN0TWFuaWZlc3RUb1NXUGx1Z2luKCksIGJyb3RsaSgpXSk7XG4gICAgICB9XG4gICAgfVxuICB9O1xufVxuXG5mdW5jdGlvbiBzdGF0c0V4dHJhY3RlclBsdWdpbigpOiBQbHVnaW5PcHRpb24ge1xuICBmdW5jdGlvbiBjb2xsZWN0VGhlbWVKc29uc0luRnJvbnRlbmQodGhlbWVKc29uQ29udGVudHM6IFJlY29yZDxzdHJpbmcsIHN0cmluZz4sIHRoZW1lTmFtZTogc3RyaW5nKSB7XG4gICAgY29uc3QgdGhlbWVKc29uID0gcGF0aC5yZXNvbHZlKGZyb250ZW5kRm9sZGVyLCBzZXR0aW5ncy50aGVtZUZvbGRlciwgdGhlbWVOYW1lLCAndGhlbWUuanNvbicpO1xuICAgIGlmIChleGlzdHNTeW5jKHRoZW1lSnNvbikpIHtcbiAgICAgIGNvbnN0IHRoZW1lSnNvbkNvbnRlbnQgPSByZWFkRmlsZVN5bmModGhlbWVKc29uLCB7IGVuY29kaW5nOiAndXRmLTgnIH0pLnJlcGxhY2UoL1xcclxcbi9nLCAnXFxuJyk7XG4gICAgICB0aGVtZUpzb25Db250ZW50c1t0aGVtZU5hbWVdID0gdGhlbWVKc29uQ29udGVudDtcbiAgICAgIGNvbnN0IHRoZW1lSnNvbk9iamVjdCA9IEpTT04ucGFyc2UodGhlbWVKc29uQ29udGVudCk7XG4gICAgICBpZiAodGhlbWVKc29uT2JqZWN0LnBhcmVudCkge1xuICAgICAgICBjb2xsZWN0VGhlbWVKc29uc0luRnJvbnRlbmQodGhlbWVKc29uQ29udGVudHMsIHRoZW1lSnNvbk9iamVjdC5wYXJlbnQpO1xuICAgICAgfVxuICAgIH1cbiAgfVxuXG4gIHJldHVybiB7XG4gICAgbmFtZTogJ3ZhYWRpbjpzdGF0cycsXG4gICAgZW5mb3JjZTogJ3Bvc3QnLFxuICAgIGFzeW5jIHdyaXRlQnVuZGxlKG9wdGlvbnM6IE91dHB1dE9wdGlvbnMsIGJ1bmRsZTogeyBbZmlsZU5hbWU6IHN0cmluZ106IEFzc2V0SW5mbyB8IENodW5rSW5mbyB9KSB7XG4gICAgICBjb25zdCBtb2R1bGVzID0gT2JqZWN0LnZhbHVlcyhidW5kbGUpLmZsYXRNYXAoKGIpID0+IChiLm1vZHVsZXMgPyBPYmplY3Qua2V5cyhiLm1vZHVsZXMpIDogW10pKTtcbiAgICAgIGNvbnN0IG5vZGVNb2R1bGVzRm9sZGVycyA9IG1vZHVsZXNcbiAgICAgICAgLm1hcCgoaWQpID0+IGlkLnJlcGxhY2UoL1xcXFwvZywgJy8nKSlcbiAgICAgICAgLmZpbHRlcigoaWQpID0+IGlkLnN0YXJ0c1dpdGgobm9kZU1vZHVsZXNGb2xkZXIucmVwbGFjZSgvXFxcXC9nLCAnLycpKSlcbiAgICAgICAgLm1hcCgoaWQpID0+IGlkLnN1YnN0cmluZyhub2RlTW9kdWxlc0ZvbGRlci5sZW5ndGggKyAxKSk7XG4gICAgICBjb25zdCBucG1Nb2R1bGVzID0gbm9kZU1vZHVsZXNGb2xkZXJzXG4gICAgICAgIC5tYXAoKGlkKSA9PiBpZC5yZXBsYWNlKC9cXFxcL2csICcvJykpXG4gICAgICAgIC5tYXAoKGlkKSA9PiB7XG4gICAgICAgICAgY29uc3QgcGFydHMgPSBpZC5zcGxpdCgnLycpO1xuICAgICAgICAgIGlmIChpZC5zdGFydHNXaXRoKCdAJykpIHtcbiAgICAgICAgICAgIHJldHVybiBwYXJ0c1swXSArICcvJyArIHBhcnRzWzFdO1xuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICByZXR1cm4gcGFydHNbMF07XG4gICAgICAgICAgfVxuICAgICAgICB9KVxuICAgICAgICAuc29ydCgpXG4gICAgICAgIC5maWx0ZXIoKHZhbHVlLCBpbmRleCwgc2VsZikgPT4gc2VsZi5pbmRleE9mKHZhbHVlKSA9PT0gaW5kZXgpO1xuICAgICAgY29uc3QgbnBtTW9kdWxlQW5kVmVyc2lvbiA9IE9iamVjdC5mcm9tRW50cmllcyhucG1Nb2R1bGVzLm1hcCgobW9kdWxlKSA9PiBbbW9kdWxlLCBnZXRWZXJzaW9uKG1vZHVsZSldKSk7XG4gICAgICBjb25zdCBjdmRscyA9IE9iamVjdC5mcm9tRW50cmllcyhcbiAgICAgICAgbnBtTW9kdWxlc1xuICAgICAgICAgIC5maWx0ZXIoKG1vZHVsZSkgPT4gZ2V0Q3ZkbE5hbWUobW9kdWxlKSAhPSBudWxsKVxuICAgICAgICAgIC5tYXAoKG1vZHVsZSkgPT4gW21vZHVsZSwgeyBuYW1lOiBnZXRDdmRsTmFtZShtb2R1bGUpLCB2ZXJzaW9uOiBnZXRWZXJzaW9uKG1vZHVsZSkgfV0pXG4gICAgICApO1xuXG4gICAgICBta2RpclN5bmMocGF0aC5kaXJuYW1lKHN0YXRzRmlsZSksIHsgcmVjdXJzaXZlOiB0cnVlIH0pO1xuICAgICAgY29uc3QgcHJvamVjdFBhY2thZ2VKc29uID0gSlNPTi5wYXJzZShyZWFkRmlsZVN5bmMocHJvamVjdFBhY2thZ2VKc29uRmlsZSwgeyBlbmNvZGluZzogJ3V0Zi04JyB9KSk7XG5cbiAgICAgIGNvbnN0IGVudHJ5U2NyaXB0cyA9IE9iamVjdC52YWx1ZXMoYnVuZGxlKVxuICAgICAgICAuZmlsdGVyKChidW5kbGUpID0+IGJ1bmRsZS5pc0VudHJ5KVxuICAgICAgICAubWFwKChidW5kbGUpID0+IGJ1bmRsZS5maWxlTmFtZSk7XG5cbiAgICAgIGNvbnN0IGdlbmVyYXRlZEluZGV4SHRtbCA9IHBhdGgucmVzb2x2ZShidWlsZE91dHB1dEZvbGRlciwgJ2luZGV4Lmh0bWwnKTtcbiAgICAgIGNvbnN0IGN1c3RvbUluZGV4RGF0YTogc3RyaW5nID0gcmVhZEZpbGVTeW5jKHByb2plY3RJbmRleEh0bWwsIHsgZW5jb2Rpbmc6ICd1dGYtOCcgfSk7XG4gICAgICBjb25zdCBnZW5lcmF0ZWRJbmRleERhdGE6IHN0cmluZyA9IHJlYWRGaWxlU3luYyhnZW5lcmF0ZWRJbmRleEh0bWwsIHtcbiAgICAgICAgZW5jb2Rpbmc6ICd1dGYtOCdcbiAgICAgIH0pO1xuXG4gICAgICBjb25zdCBjdXN0b21JbmRleFJvd3MgPSBuZXcgU2V0KGN1c3RvbUluZGV4RGF0YS5zcGxpdCgvW1xcclxcbl0vKS5maWx0ZXIoKHJvdykgPT4gcm93LnRyaW0oKSAhPT0gJycpKTtcbiAgICAgIGNvbnN0IGdlbmVyYXRlZEluZGV4Um93cyA9IGdlbmVyYXRlZEluZGV4RGF0YS5zcGxpdCgvW1xcclxcbl0vKS5maWx0ZXIoKHJvdykgPT4gcm93LnRyaW0oKSAhPT0gJycpO1xuXG4gICAgICBjb25zdCByb3dzR2VuZXJhdGVkOiBzdHJpbmdbXSA9IFtdO1xuICAgICAgZ2VuZXJhdGVkSW5kZXhSb3dzLmZvckVhY2goKHJvdykgPT4ge1xuICAgICAgICBpZiAoIWN1c3RvbUluZGV4Um93cy5oYXMocm93KSkge1xuICAgICAgICAgIHJvd3NHZW5lcmF0ZWQucHVzaChyb3cpO1xuICAgICAgICB9XG4gICAgICB9KTtcblxuICAgICAgLy9BZnRlciBkZXYtYnVuZGxlIGJ1aWxkIGFkZCB1c2VkIEZsb3cgZnJvbnRlbmQgaW1wb3J0cyBKc01vZHVsZS9KYXZhU2NyaXB0L0Nzc0ltcG9ydFxuXG4gICAgICBjb25zdCBwYXJzZUltcG9ydHMgPSAoZmlsZW5hbWU6IHN0cmluZywgcmVzdWx0OiBTZXQ8c3RyaW5nPik6IHZvaWQgPT4ge1xuICAgICAgICBjb25zdCBjb250ZW50OiBzdHJpbmcgPSByZWFkRmlsZVN5bmMoZmlsZW5hbWUsIHsgZW5jb2Rpbmc6ICd1dGYtOCcgfSk7XG4gICAgICAgIGNvbnN0IGxpbmVzID0gY29udGVudC5zcGxpdCgnXFxuJyk7XG4gICAgICAgIGNvbnN0IHN0YXRpY0ltcG9ydHMgPSBsaW5lc1xuICAgICAgICAgIC5maWx0ZXIoKGxpbmUpID0+IGxpbmUuc3RhcnRzV2l0aCgnaW1wb3J0ICcpKVxuICAgICAgICAgIC5tYXAoKGxpbmUpID0+IGxpbmUuc3Vic3RyaW5nKGxpbmUuaW5kZXhPZihcIidcIikgKyAxLCBsaW5lLmxhc3RJbmRleE9mKFwiJ1wiKSkpXG4gICAgICAgICAgLm1hcCgobGluZSkgPT4gKGxpbmUuaW5jbHVkZXMoJz8nKSA/IGxpbmUuc3Vic3RyaW5nKDAsIGxpbmUubGFzdEluZGV4T2YoJz8nKSkgOiBsaW5lKSk7XG4gICAgICAgIGNvbnN0IGR5bmFtaWNJbXBvcnRzID0gbGluZXNcbiAgICAgICAgICAuZmlsdGVyKChsaW5lKSA9PiBsaW5lLmluY2x1ZGVzKCdpbXBvcnQoJykpXG4gICAgICAgICAgLm1hcCgobGluZSkgPT4gbGluZS5yZXBsYWNlKC8uKmltcG9ydFxcKC8sICcnKSlcbiAgICAgICAgICAubWFwKChsaW5lKSA9PiBsaW5lLnNwbGl0KC8nLylbMV0pXG4gICAgICAgICAgLm1hcCgobGluZSkgPT4gKGxpbmUuaW5jbHVkZXMoJz8nKSA/IGxpbmUuc3Vic3RyaW5nKDAsIGxpbmUubGFzdEluZGV4T2YoJz8nKSkgOiBsaW5lKSk7XG5cbiAgICAgICAgc3RhdGljSW1wb3J0cy5mb3JFYWNoKChzdGF0aWNJbXBvcnQpID0+IHJlc3VsdC5hZGQoc3RhdGljSW1wb3J0KSk7XG5cbiAgICAgICAgZHluYW1pY0ltcG9ydHMubWFwKChkeW5hbWljSW1wb3J0KSA9PiB7XG4gICAgICAgICAgY29uc3QgaW1wb3J0ZWRGaWxlID0gcGF0aC5yZXNvbHZlKHBhdGguZGlybmFtZShmaWxlbmFtZSksIGR5bmFtaWNJbXBvcnQpO1xuICAgICAgICAgIHBhcnNlSW1wb3J0cyhpbXBvcnRlZEZpbGUsIHJlc3VsdCk7XG4gICAgICAgIH0pO1xuICAgICAgfTtcblxuICAgICAgY29uc3QgZ2VuZXJhdGVkSW1wb3J0c1NldCA9IG5ldyBTZXQ8c3RyaW5nPigpO1xuICAgICAgcGFyc2VJbXBvcnRzKFxuICAgICAgICBwYXRoLnJlc29sdmUodGhlbWVPcHRpb25zLmZyb250ZW5kR2VuZXJhdGVkRm9sZGVyLCAnZmxvdycsICdnZW5lcmF0ZWQtZmxvdy1pbXBvcnRzLmpzJyksXG4gICAgICAgIGdlbmVyYXRlZEltcG9ydHNTZXRcbiAgICAgICk7XG4gICAgICBjb25zdCBnZW5lcmF0ZWRJbXBvcnRzID0gQXJyYXkuZnJvbShnZW5lcmF0ZWRJbXBvcnRzU2V0KS5zb3J0KCk7XG5cbiAgICAgIGNvbnN0IGZyb250ZW5kRmlsZXM6IFJlY29yZDxzdHJpbmcsIHN0cmluZz4gPSB7fTtcblxuICAgICAgY29uc3QgcHJvamVjdEZpbGVFeHRlbnNpb25zID0gWycuanMnLCAnLmpzLm1hcCcsICcudHMnLCAnLnRzLm1hcCcsICcudHN4JywgJy50c3gubWFwJywgJy5jc3MnLCAnLmNzcy5tYXAnXTtcblxuICAgICAgY29uc3QgaXNUaGVtZUNvbXBvbmVudHNSZXNvdXJjZSA9IChpZDogc3RyaW5nKSA9PlxuICAgICAgICAgIGlkLnN0YXJ0c1dpdGgodGhlbWVPcHRpb25zLmZyb250ZW5kR2VuZXJhdGVkRm9sZGVyLnJlcGxhY2UoL1xcXFwvZywgJy8nKSlcbiAgICAgICAgICAgICAgJiYgaWQubWF0Y2goLy4qXFwvamFyLXJlc291cmNlc1xcL3RoZW1lc1xcL1teXFwvXStcXC9jb21wb25lbnRzXFwvLyk7XG5cbiAgICAgIGNvbnN0IGlzR2VuZXJhdGVkV2ViQ29tcG9uZW50UmVzb3VyY2UgPSAoaWQ6IHN0cmluZykgPT5cbiAgICAgICAgICBpZC5zdGFydHNXaXRoKHRoZW1lT3B0aW9ucy5mcm9udGVuZEdlbmVyYXRlZEZvbGRlci5yZXBsYWNlKC9cXFxcL2csICcvJykpXG4gICAgICAgICAgICAgICYmIGlkLm1hdGNoKC8uKlxcL2Zsb3dcXC93ZWItY29tcG9uZW50c1xcLy8pO1xuXG4gICAgICBjb25zdCBpc0Zyb250ZW5kUmVzb3VyY2VDb2xsZWN0ZWQgPSAoaWQ6IHN0cmluZykgPT5cbiAgICAgICAgICAhaWQuc3RhcnRzV2l0aCh0aGVtZU9wdGlvbnMuZnJvbnRlbmRHZW5lcmF0ZWRGb2xkZXIucmVwbGFjZSgvXFxcXC9nLCAnLycpKVxuICAgICAgICAgIHx8IGlzVGhlbWVDb21wb25lbnRzUmVzb3VyY2UoaWQpXG4gICAgICAgICAgfHwgaXNHZW5lcmF0ZWRXZWJDb21wb25lbnRSZXNvdXJjZShpZCk7XG5cbiAgICAgIC8vIGNvbGxlY3RzIHByb2plY3QncyBmcm9udGVuZCByZXNvdXJjZXMgaW4gZnJvbnRlbmQgZm9sZGVyLCBleGNsdWRpbmdcbiAgICAgIC8vICdnZW5lcmF0ZWQnIHN1Yi1mb2xkZXIsIGV4Y2VwdCBmb3IgbGVnYWN5IHNoYWRvdyBET00gc3R5bGVzaGVldHNcbiAgICAgIC8vIHBhY2thZ2VkIGluIGB0aGVtZS9jb21wb25lbnRzL2AgZm9sZGVyXG4gICAgICAvLyBhbmQgZ2VuZXJhdGVkIHdlYiBjb21wb25lbnQgcmVzb3VyY2VzIGluIGBmbG93L3dlYi1jb21wb25lbnRzYCBmb2xkZXIuXG4gICAgICBtb2R1bGVzXG4gICAgICAgIC5tYXAoKGlkKSA9PiBpZC5yZXBsYWNlKC9cXFxcL2csICcvJykpXG4gICAgICAgIC5maWx0ZXIoKGlkKSA9PiBpZC5zdGFydHNXaXRoKGZyb250ZW5kRm9sZGVyLnJlcGxhY2UoL1xcXFwvZywgJy8nKSkpXG4gICAgICAgIC5maWx0ZXIoaXNGcm9udGVuZFJlc291cmNlQ29sbGVjdGVkKVxuICAgICAgICAubWFwKChpZCkgPT4gaWQuc3Vic3RyaW5nKGZyb250ZW5kRm9sZGVyLmxlbmd0aCArIDEpKVxuICAgICAgICAubWFwKChsaW5lOiBzdHJpbmcpID0+IChsaW5lLmluY2x1ZGVzKCc/JykgPyBsaW5lLnN1YnN0cmluZygwLCBsaW5lLmxhc3RJbmRleE9mKCc/JykpIDogbGluZSkpXG4gICAgICAgIC5mb3JFYWNoKChsaW5lOiBzdHJpbmcpID0+IHtcbiAgICAgICAgICAvLyBcXHJcXG4gZnJvbSB3aW5kb3dzIG1hZGUgZmlsZXMgbWF5IGJlIHVzZWQgc28gY2hhbmdlIHRvIFxcblxuICAgICAgICAgIGNvbnN0IGZpbGVQYXRoID0gcGF0aC5yZXNvbHZlKGZyb250ZW5kRm9sZGVyLCBsaW5lKTtcbiAgICAgICAgICBpZiAocHJvamVjdEZpbGVFeHRlbnNpb25zLmluY2x1ZGVzKHBhdGguZXh0bmFtZShmaWxlUGF0aCkpKSB7XG4gICAgICAgICAgICBjb25zdCBmaWxlQnVmZmVyID0gcmVhZEZpbGVTeW5jKGZpbGVQYXRoLCB7IGVuY29kaW5nOiAndXRmLTgnIH0pLnJlcGxhY2UoL1xcclxcbi9nLCAnXFxuJyk7XG4gICAgICAgICAgICBmcm9udGVuZEZpbGVzW2xpbmVdID0gY3JlYXRlSGFzaCgnc2hhMjU2JykudXBkYXRlKGZpbGVCdWZmZXIsICd1dGY4JykuZGlnZXN0KCdoZXgnKTtcbiAgICAgICAgICB9XG4gICAgICAgIH0pO1xuXG4gICAgICAvLyBjb2xsZWN0cyBmcm9udGVuZCByZXNvdXJjZXMgZnJvbSB0aGUgSkFSc1xuICAgICAgZ2VuZXJhdGVkSW1wb3J0c1xuICAgICAgICAuZmlsdGVyKChsaW5lOiBzdHJpbmcpID0+IGxpbmUuaW5jbHVkZXMoJ2dlbmVyYXRlZC9qYXItcmVzb3VyY2VzJykpXG4gICAgICAgIC5mb3JFYWNoKChsaW5lOiBzdHJpbmcpID0+IHtcbiAgICAgICAgICBsZXQgZmlsZW5hbWUgPSBsaW5lLnN1YnN0cmluZyhsaW5lLmluZGV4T2YoJ2dlbmVyYXRlZCcpKTtcbiAgICAgICAgICAvLyBcXHJcXG4gZnJvbSB3aW5kb3dzIG1hZGUgZmlsZXMgbWF5IGJlIHVzZWQgcm8gcmVtb3ZlIHRvIGJlIG9ubHkgXFxuXG4gICAgICAgICAgY29uc3QgZmlsZUJ1ZmZlciA9IHJlYWRGaWxlU3luYyhwYXRoLnJlc29sdmUoZnJvbnRlbmRGb2xkZXIsIGZpbGVuYW1lKSwgeyBlbmNvZGluZzogJ3V0Zi04JyB9KS5yZXBsYWNlKFxuICAgICAgICAgICAgL1xcclxcbi9nLFxuICAgICAgICAgICAgJ1xcbidcbiAgICAgICAgICApO1xuICAgICAgICAgIGNvbnN0IGhhc2ggPSBjcmVhdGVIYXNoKCdzaGEyNTYnKS51cGRhdGUoZmlsZUJ1ZmZlciwgJ3V0ZjgnKS5kaWdlc3QoJ2hleCcpO1xuXG4gICAgICAgICAgY29uc3QgZmlsZUtleSA9IGxpbmUuc3Vic3RyaW5nKGxpbmUuaW5kZXhPZignamFyLXJlc291cmNlcy8nKSArIDE0KTtcbiAgICAgICAgICBmcm9udGVuZEZpbGVzW2ZpbGVLZXldID0gaGFzaDtcbiAgICAgICAgfSk7XG4gICAgICAvLyBjb2xsZWN0cyBhbmQgaGFzaCByZXN0IG9mIHRoZSBGcm9udGVuZCByZXNvdXJjZXMgZXhjbHVkaW5nIGZpbGVzIGluIC9nZW5lcmF0ZWQvIGFuZCAvdGhlbWVzL1xuICAgICAgLy8gYW5kIGZpbGVzIGFscmVhZHkgaW4gZnJvbnRlbmRGaWxlcy5cbiAgICAgIGxldCBmcm9udGVuZEZvbGRlckFsaWFzID0gXCJGcm9udGVuZFwiO1xuICAgICAgZ2VuZXJhdGVkSW1wb3J0c1xuICAgICAgICAuZmlsdGVyKChsaW5lOiBzdHJpbmcpID0+IGxpbmUuc3RhcnRzV2l0aChmcm9udGVuZEZvbGRlckFsaWFzICsgJy8nKSlcbiAgICAgICAgLmZpbHRlcigobGluZTogc3RyaW5nKSA9PiAhbGluZS5zdGFydHNXaXRoKGZyb250ZW5kRm9sZGVyQWxpYXMgKyAnL2dlbmVyYXRlZC8nKSlcbiAgICAgICAgLmZpbHRlcigobGluZTogc3RyaW5nKSA9PiAhbGluZS5zdGFydHNXaXRoKGZyb250ZW5kRm9sZGVyQWxpYXMgKyAnL3RoZW1lcy8nKSlcbiAgICAgICAgLm1hcCgobGluZSkgPT4gbGluZS5zdWJzdHJpbmcoZnJvbnRlbmRGb2xkZXJBbGlhcy5sZW5ndGggKyAxKSlcbiAgICAgICAgLmZpbHRlcigobGluZTogc3RyaW5nKSA9PiAhZnJvbnRlbmRGaWxlc1tsaW5lXSlcbiAgICAgICAgLmZvckVhY2goKGxpbmU6IHN0cmluZykgPT4ge1xuICAgICAgICAgIGNvbnN0IGZpbGVQYXRoID0gcGF0aC5yZXNvbHZlKGZyb250ZW5kRm9sZGVyLCBsaW5lKTtcbiAgICAgICAgICBpZiAocHJvamVjdEZpbGVFeHRlbnNpb25zLmluY2x1ZGVzKHBhdGguZXh0bmFtZShmaWxlUGF0aCkpICYmIGV4aXN0c1N5bmMoZmlsZVBhdGgpKSB7XG4gICAgICAgICAgICBjb25zdCBmaWxlQnVmZmVyID0gcmVhZEZpbGVTeW5jKGZpbGVQYXRoLCB7IGVuY29kaW5nOiAndXRmLTgnIH0pLnJlcGxhY2UoL1xcclxcbi9nLCAnXFxuJyk7XG4gICAgICAgICAgICBmcm9udGVuZEZpbGVzW2xpbmVdID0gY3JlYXRlSGFzaCgnc2hhMjU2JykudXBkYXRlKGZpbGVCdWZmZXIsICd1dGY4JykuZGlnZXN0KCdoZXgnKTtcbiAgICAgICAgICB9XG4gICAgICAgIH0pO1xuICAgICAgLy8gSWYgYSBpbmRleC50cyBleGlzdHMgaGFzaCBpdCB0byBiZSBhYmxlIHRvIHNlZSBpZiBpdCBjaGFuZ2VzLlxuICAgICAgaWYgKGV4aXN0c1N5bmMocGF0aC5yZXNvbHZlKGZyb250ZW5kRm9sZGVyLCAnaW5kZXgudHMnKSkpIHtcbiAgICAgICAgY29uc3QgZmlsZUJ1ZmZlciA9IHJlYWRGaWxlU3luYyhwYXRoLnJlc29sdmUoZnJvbnRlbmRGb2xkZXIsICdpbmRleC50cycpLCB7IGVuY29kaW5nOiAndXRmLTgnIH0pLnJlcGxhY2UoXG4gICAgICAgICAgL1xcclxcbi9nLFxuICAgICAgICAgICdcXG4nXG4gICAgICAgICk7XG4gICAgICAgIGZyb250ZW5kRmlsZXNbYGluZGV4LnRzYF0gPSBjcmVhdGVIYXNoKCdzaGEyNTYnKS51cGRhdGUoZmlsZUJ1ZmZlciwgJ3V0ZjgnKS5kaWdlc3QoJ2hleCcpO1xuICAgICAgfVxuXG4gICAgICBjb25zdCB0aGVtZUpzb25Db250ZW50czogUmVjb3JkPHN0cmluZywgc3RyaW5nPiA9IHt9O1xuICAgICAgY29uc3QgdGhlbWVzRm9sZGVyID0gcGF0aC5yZXNvbHZlKGphclJlc291cmNlc0ZvbGRlciwgJ3RoZW1lcycpO1xuICAgICAgaWYgKGV4aXN0c1N5bmModGhlbWVzRm9sZGVyKSkge1xuICAgICAgICByZWFkZGlyU3luYyh0aGVtZXNGb2xkZXIpLmZvckVhY2goKHRoZW1lRm9sZGVyKSA9PiB7XG4gICAgICAgICAgY29uc3QgdGhlbWVKc29uID0gcGF0aC5yZXNvbHZlKHRoZW1lc0ZvbGRlciwgdGhlbWVGb2xkZXIsICd0aGVtZS5qc29uJyk7XG4gICAgICAgICAgaWYgKGV4aXN0c1N5bmModGhlbWVKc29uKSkge1xuICAgICAgICAgICAgdGhlbWVKc29uQ29udGVudHNbcGF0aC5iYXNlbmFtZSh0aGVtZUZvbGRlcildID0gcmVhZEZpbGVTeW5jKHRoZW1lSnNvbiwgeyBlbmNvZGluZzogJ3V0Zi04JyB9KS5yZXBsYWNlKFxuICAgICAgICAgICAgICAvXFxyXFxuL2csXG4gICAgICAgICAgICAgICdcXG4nXG4gICAgICAgICAgICApO1xuICAgICAgICAgIH1cbiAgICAgICAgfSk7XG4gICAgICB9XG5cbiAgICAgIGNvbGxlY3RUaGVtZUpzb25zSW5Gcm9udGVuZCh0aGVtZUpzb25Db250ZW50cywgc2V0dGluZ3MudGhlbWVOYW1lKTtcblxuICAgICAgbGV0IHdlYkNvbXBvbmVudHM6IHN0cmluZ1tdID0gW107XG4gICAgICBpZiAod2ViQ29tcG9uZW50VGFncykge1xuICAgICAgICB3ZWJDb21wb25lbnRzID0gd2ViQ29tcG9uZW50VGFncy5zcGxpdCgnOycpO1xuICAgICAgfVxuXG4gICAgICBjb25zdCBzdGF0cyA9IHtcbiAgICAgICAgcGFja2FnZUpzb25EZXBlbmRlbmNpZXM6IHByb2plY3RQYWNrYWdlSnNvbi5kZXBlbmRlbmNpZXMsXG4gICAgICAgIG5wbU1vZHVsZXM6IG5wbU1vZHVsZUFuZFZlcnNpb24sXG4gICAgICAgIGJ1bmRsZUltcG9ydHM6IGdlbmVyYXRlZEltcG9ydHMsXG4gICAgICAgIGZyb250ZW5kSGFzaGVzOiBmcm9udGVuZEZpbGVzLFxuICAgICAgICB0aGVtZUpzb25Db250ZW50czogdGhlbWVKc29uQ29udGVudHMsXG4gICAgICAgIGVudHJ5U2NyaXB0cyxcbiAgICAgICAgd2ViQ29tcG9uZW50cyxcbiAgICAgICAgY3ZkbE1vZHVsZXM6IGN2ZGxzLFxuICAgICAgICBwYWNrYWdlSnNvbkhhc2g6IHByb2plY3RQYWNrYWdlSnNvbj8udmFhZGluPy5oYXNoLFxuICAgICAgICBpbmRleEh0bWxHZW5lcmF0ZWQ6IHJvd3NHZW5lcmF0ZWRcbiAgICAgIH07XG4gICAgICB3cml0ZUZpbGVTeW5jKHN0YXRzRmlsZSwgSlNPTi5zdHJpbmdpZnkoc3RhdHMsIG51bGwsIDEpKTtcbiAgICB9XG4gIH07XG59XG5mdW5jdGlvbiB2YWFkaW5CdW5kbGVzUGx1Z2luKCk6IFBsdWdpbk9wdGlvbiB7XG4gIHR5cGUgRXhwb3J0SW5mbyA9XG4gICAgfCBzdHJpbmdcbiAgICB8IHtcbiAgICAgICAgbmFtZXNwYWNlPzogc3RyaW5nO1xuICAgICAgICBzb3VyY2U6IHN0cmluZztcbiAgICAgIH07XG5cbiAgdHlwZSBFeHBvc2VJbmZvID0ge1xuICAgIGV4cG9ydHM6IEV4cG9ydEluZm9bXTtcbiAgfTtcblxuICB0eXBlIFBhY2thZ2VJbmZvID0ge1xuICAgIHZlcnNpb246IHN0cmluZztcbiAgICBleHBvc2VzOiBSZWNvcmQ8c3RyaW5nLCBFeHBvc2VJbmZvPjtcbiAgfTtcblxuICB0eXBlIEJ1bmRsZUpzb24gPSB7XG4gICAgcGFja2FnZXM6IFJlY29yZDxzdHJpbmcsIFBhY2thZ2VJbmZvPjtcbiAgfTtcblxuICBjb25zdCBkaXNhYmxlZE1lc3NhZ2UgPSAnVmFhZGluIGNvbXBvbmVudCBkZXBlbmRlbmN5IGJ1bmRsZXMgYXJlIGRpc2FibGVkLic7XG5cbiAgY29uc3QgbW9kdWxlc0RpcmVjdG9yeSA9IG5vZGVNb2R1bGVzRm9sZGVyLnJlcGxhY2UoL1xcXFwvZywgJy8nKTtcblxuICBsZXQgdmFhZGluQnVuZGxlSnNvbjogQnVuZGxlSnNvbjtcblxuICBmdW5jdGlvbiBwYXJzZU1vZHVsZUlkKGlkOiBzdHJpbmcpOiB7IHBhY2thZ2VOYW1lOiBzdHJpbmc7IG1vZHVsZVBhdGg6IHN0cmluZyB9IHtcbiAgICBjb25zdCBbc2NvcGUsIHNjb3BlZFBhY2thZ2VOYW1lXSA9IGlkLnNwbGl0KCcvJywgMyk7XG4gICAgY29uc3QgcGFja2FnZU5hbWUgPSBzY29wZS5zdGFydHNXaXRoKCdAJykgPyBgJHtzY29wZX0vJHtzY29wZWRQYWNrYWdlTmFtZX1gIDogc2NvcGU7XG4gICAgY29uc3QgbW9kdWxlUGF0aCA9IGAuJHtpZC5zdWJzdHJpbmcocGFja2FnZU5hbWUubGVuZ3RoKX1gO1xuICAgIHJldHVybiB7XG4gICAgICBwYWNrYWdlTmFtZSxcbiAgICAgIG1vZHVsZVBhdGhcbiAgICB9O1xuICB9XG5cbiAgZnVuY3Rpb24gZ2V0RXhwb3J0cyhpZDogc3RyaW5nKTogc3RyaW5nW10gfCB1bmRlZmluZWQge1xuICAgIGNvbnN0IHsgcGFja2FnZU5hbWUsIG1vZHVsZVBhdGggfSA9IHBhcnNlTW9kdWxlSWQoaWQpO1xuICAgIGNvbnN0IHBhY2thZ2VJbmZvID0gdmFhZGluQnVuZGxlSnNvbi5wYWNrYWdlc1twYWNrYWdlTmFtZV07XG5cbiAgICBpZiAoIXBhY2thZ2VJbmZvKSByZXR1cm47XG5cbiAgICBjb25zdCBleHBvc2VJbmZvOiBFeHBvc2VJbmZvID0gcGFja2FnZUluZm8uZXhwb3Nlc1ttb2R1bGVQYXRoXTtcbiAgICBpZiAoIWV4cG9zZUluZm8pIHJldHVybjtcblxuICAgIGNvbnN0IGV4cG9ydHNTZXQgPSBuZXcgU2V0PHN0cmluZz4oKTtcbiAgICBmb3IgKGNvbnN0IGUgb2YgZXhwb3NlSW5mby5leHBvcnRzKSB7XG4gICAgICBpZiAodHlwZW9mIGUgPT09ICdzdHJpbmcnKSB7XG4gICAgICAgIGV4cG9ydHNTZXQuYWRkKGUpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgY29uc3QgeyBuYW1lc3BhY2UsIHNvdXJjZSB9ID0gZTtcbiAgICAgICAgaWYgKG5hbWVzcGFjZSkge1xuICAgICAgICAgIGV4cG9ydHNTZXQuYWRkKG5hbWVzcGFjZSk7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgY29uc3Qgc291cmNlRXhwb3J0cyA9IGdldEV4cG9ydHMoc291cmNlKTtcbiAgICAgICAgICBpZiAoc291cmNlRXhwb3J0cykge1xuICAgICAgICAgICAgc291cmNlRXhwb3J0cy5mb3JFYWNoKChlKSA9PiBleHBvcnRzU2V0LmFkZChlKSk7XG4gICAgICAgICAgfVxuICAgICAgICB9XG4gICAgICB9XG4gICAgfVxuICAgIHJldHVybiBBcnJheS5mcm9tKGV4cG9ydHNTZXQpO1xuICB9XG5cbiAgZnVuY3Rpb24gZ2V0RXhwb3J0QmluZGluZyhiaW5kaW5nOiBzdHJpbmcpIHtcbiAgICByZXR1cm4gYmluZGluZyA9PT0gJ2RlZmF1bHQnID8gJ19kZWZhdWx0IGFzIGRlZmF1bHQnIDogYmluZGluZztcbiAgfVxuXG4gIGZ1bmN0aW9uIGdldEltcG9ydEFzc2lnbWVudChiaW5kaW5nOiBzdHJpbmcpIHtcbiAgICByZXR1cm4gYmluZGluZyA9PT0gJ2RlZmF1bHQnID8gJ2RlZmF1bHQ6IF9kZWZhdWx0JyA6IGJpbmRpbmc7XG4gIH1cblxuICByZXR1cm4ge1xuICAgIG5hbWU6ICd2YWFkaW46YnVuZGxlcycsXG4gICAgZW5mb3JjZTogJ3ByZScsXG4gICAgYXBwbHkoY29uZmlnLCB7IGNvbW1hbmQgfSkge1xuICAgICAgaWYgKGNvbW1hbmQgIT09ICdzZXJ2ZScpIHJldHVybiBmYWxzZTtcblxuICAgICAgdHJ5IHtcbiAgICAgICAgY29uc3QgdmFhZGluQnVuZGxlSnNvblBhdGggPSByZXF1aXJlLnJlc29sdmUoJ0B2YWFkaW4vYnVuZGxlcy92YWFkaW4tYnVuZGxlLmpzb24nKTtcbiAgICAgICAgdmFhZGluQnVuZGxlSnNvbiA9IEpTT04ucGFyc2UocmVhZEZpbGVTeW5jKHZhYWRpbkJ1bmRsZUpzb25QYXRoLCB7IGVuY29kaW5nOiAndXRmOCcgfSkpO1xuICAgICAgfSBjYXRjaCAoZTogdW5rbm93bikge1xuICAgICAgICBpZiAodHlwZW9mIGUgPT09ICdvYmplY3QnICYmIChlIGFzIHsgY29kZTogc3RyaW5nIH0pLmNvZGUgPT09ICdNT0RVTEVfTk9UX0ZPVU5EJykge1xuICAgICAgICAgIHZhYWRpbkJ1bmRsZUpzb24gPSB7IHBhY2thZ2VzOiB7fSB9O1xuICAgICAgICAgIGNvbnNvbGUuaW5mbyhgQHZhYWRpbi9idW5kbGVzIG5wbSBwYWNrYWdlIGlzIG5vdCBmb3VuZCwgJHtkaXNhYmxlZE1lc3NhZ2V9YCk7XG4gICAgICAgICAgcmV0dXJuIGZhbHNlO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIHRocm93IGU7XG4gICAgICAgIH1cbiAgICAgIH1cblxuICAgICAgY29uc3QgdmVyc2lvbk1pc21hdGNoZXM6IEFycmF5PHsgbmFtZTogc3RyaW5nOyBidW5kbGVkVmVyc2lvbjogc3RyaW5nOyBpbnN0YWxsZWRWZXJzaW9uOiBzdHJpbmcgfT4gPSBbXTtcbiAgICAgIGZvciAoY29uc3QgW25hbWUsIHBhY2thZ2VJbmZvXSBvZiBPYmplY3QuZW50cmllcyh2YWFkaW5CdW5kbGVKc29uLnBhY2thZ2VzKSkge1xuICAgICAgICBsZXQgaW5zdGFsbGVkVmVyc2lvbjogc3RyaW5nIHwgdW5kZWZpbmVkID0gdW5kZWZpbmVkO1xuICAgICAgICB0cnkge1xuICAgICAgICAgIGNvbnN0IHsgdmVyc2lvbjogYnVuZGxlZFZlcnNpb24gfSA9IHBhY2thZ2VJbmZvO1xuICAgICAgICAgIGNvbnN0IGluc3RhbGxlZFBhY2thZ2VKc29uRmlsZSA9IHBhdGgucmVzb2x2ZShtb2R1bGVzRGlyZWN0b3J5LCBuYW1lLCAncGFja2FnZS5qc29uJyk7XG4gICAgICAgICAgY29uc3QgcGFja2FnZUpzb24gPSBKU09OLnBhcnNlKHJlYWRGaWxlU3luYyhpbnN0YWxsZWRQYWNrYWdlSnNvbkZpbGUsIHsgZW5jb2Rpbmc6ICd1dGY4JyB9KSk7XG4gICAgICAgICAgaW5zdGFsbGVkVmVyc2lvbiA9IHBhY2thZ2VKc29uLnZlcnNpb247XG4gICAgICAgICAgaWYgKGluc3RhbGxlZFZlcnNpb24gJiYgaW5zdGFsbGVkVmVyc2lvbiAhPT0gYnVuZGxlZFZlcnNpb24pIHtcbiAgICAgICAgICAgIHZlcnNpb25NaXNtYXRjaGVzLnB1c2goe1xuICAgICAgICAgICAgICBuYW1lLFxuICAgICAgICAgICAgICBidW5kbGVkVmVyc2lvbixcbiAgICAgICAgICAgICAgaW5zdGFsbGVkVmVyc2lvblxuICAgICAgICAgICAgfSk7XG4gICAgICAgICAgfVxuICAgICAgICB9IGNhdGNoIChfKSB7XG4gICAgICAgICAgLy8gaWdub3JlIHBhY2thZ2Ugbm90IGZvdW5kXG4gICAgICAgIH1cbiAgICAgIH1cbiAgICAgIGlmICh2ZXJzaW9uTWlzbWF0Y2hlcy5sZW5ndGgpIHtcbiAgICAgICAgY29uc29sZS5pbmZvKGBAdmFhZGluL2J1bmRsZXMgaGFzIHZlcnNpb24gbWlzbWF0Y2hlcyB3aXRoIGluc3RhbGxlZCBwYWNrYWdlcywgJHtkaXNhYmxlZE1lc3NhZ2V9YCk7XG4gICAgICAgIGNvbnNvbGUuaW5mbyhgUGFja2FnZXMgd2l0aCB2ZXJzaW9uIG1pc21hdGNoZXM6ICR7SlNPTi5zdHJpbmdpZnkodmVyc2lvbk1pc21hdGNoZXMsIHVuZGVmaW5lZCwgMil9YCk7XG4gICAgICAgIHZhYWRpbkJ1bmRsZUpzb24gPSB7IHBhY2thZ2VzOiB7fSB9O1xuICAgICAgICByZXR1cm4gZmFsc2U7XG4gICAgICB9XG5cbiAgICAgIHJldHVybiB0cnVlO1xuICAgIH0sXG4gICAgYXN5bmMgY29uZmlnKGNvbmZpZykge1xuICAgICAgcmV0dXJuIG1lcmdlQ29uZmlnKFxuICAgICAgICB7XG4gICAgICAgICAgb3B0aW1pemVEZXBzOiB7XG4gICAgICAgICAgICBleGNsdWRlOiBbXG4gICAgICAgICAgICAgIC8vIFZhYWRpbiBidW5kbGVcbiAgICAgICAgICAgICAgJ0B2YWFkaW4vYnVuZGxlcycsXG4gICAgICAgICAgICAgIC4uLk9iamVjdC5rZXlzKHZhYWRpbkJ1bmRsZUpzb24ucGFja2FnZXMpLFxuICAgICAgICAgICAgICAnQHZhYWRpbi92YWFkaW4tbWF0ZXJpYWwtc3R5bGVzJ1xuICAgICAgICAgICAgXVxuICAgICAgICAgIH1cbiAgICAgICAgfSxcbiAgICAgICAgY29uZmlnXG4gICAgICApO1xuICAgIH0sXG4gICAgbG9hZChyYXdJZCkge1xuICAgICAgY29uc3QgW3BhdGgsIHBhcmFtc10gPSByYXdJZC5zcGxpdCgnPycpO1xuICAgICAgaWYgKCFwYXRoLnN0YXJ0c1dpdGgobW9kdWxlc0RpcmVjdG9yeSkpIHJldHVybjtcblxuICAgICAgY29uc3QgaWQgPSBwYXRoLnN1YnN0cmluZyhtb2R1bGVzRGlyZWN0b3J5Lmxlbmd0aCArIDEpO1xuICAgICAgY29uc3QgYmluZGluZ3MgPSBnZXRFeHBvcnRzKGlkKTtcbiAgICAgIGlmIChiaW5kaW5ncyA9PT0gdW5kZWZpbmVkKSByZXR1cm47XG5cbiAgICAgIGNvbnN0IGNhY2hlU3VmZml4ID0gcGFyYW1zID8gYD8ke3BhcmFtc31gIDogJyc7XG4gICAgICBjb25zdCBidW5kbGVQYXRoID0gYEB2YWFkaW4vYnVuZGxlcy92YWFkaW4uanMke2NhY2hlU3VmZml4fWA7XG5cbiAgICAgIHJldHVybiBgaW1wb3J0IHsgaW5pdCBhcyBWYWFkaW5CdW5kbGVJbml0LCBnZXQgYXMgVmFhZGluQnVuZGxlR2V0IH0gZnJvbSAnJHtidW5kbGVQYXRofSc7XG5hd2FpdCBWYWFkaW5CdW5kbGVJbml0KCdkZWZhdWx0Jyk7XG5jb25zdCB7ICR7YmluZGluZ3MubWFwKGdldEltcG9ydEFzc2lnbWVudCkuam9pbignLCAnKX0gfSA9IChhd2FpdCBWYWFkaW5CdW5kbGVHZXQoJy4vbm9kZV9tb2R1bGVzLyR7aWR9JykpKCk7XG5leHBvcnQgeyAke2JpbmRpbmdzLm1hcChnZXRFeHBvcnRCaW5kaW5nKS5qb2luKCcsICcpfSB9O2A7XG4gICAgfVxuICB9O1xufVxuXG5mdW5jdGlvbiB0aGVtZVBsdWdpbihvcHRzKTogUGx1Z2luT3B0aW9uIHtcbiAgY29uc3QgZnVsbFRoZW1lT3B0aW9ucyA9IHsgLi4udGhlbWVPcHRpb25zLCBkZXZNb2RlOiBvcHRzLmRldk1vZGUgfTtcbiAgcmV0dXJuIHtcbiAgICBuYW1lOiAndmFhZGluOnRoZW1lJyxcbiAgICBjb25maWcoKSB7XG4gICAgICBwcm9jZXNzVGhlbWVSZXNvdXJjZXMoZnVsbFRoZW1lT3B0aW9ucywgY29uc29sZSk7XG4gICAgfSxcbiAgICBjb25maWd1cmVTZXJ2ZXIoc2VydmVyKSB7XG4gICAgICBmdW5jdGlvbiBoYW5kbGVUaGVtZUZpbGVDcmVhdGVEZWxldGUodGhlbWVGaWxlLCBzdGF0cykge1xuICAgICAgICBpZiAodGhlbWVGaWxlLnN0YXJ0c1dpdGgodGhlbWVGb2xkZXIpKSB7XG4gICAgICAgICAgY29uc3QgY2hhbmdlZCA9IHBhdGgucmVsYXRpdmUodGhlbWVGb2xkZXIsIHRoZW1lRmlsZSk7XG4gICAgICAgICAgY29uc29sZS5kZWJ1ZygnVGhlbWUgZmlsZSAnICsgKCEhc3RhdHMgPyAnY3JlYXRlZCcgOiAnZGVsZXRlZCcpLCBjaGFuZ2VkKTtcbiAgICAgICAgICBwcm9jZXNzVGhlbWVSZXNvdXJjZXMoZnVsbFRoZW1lT3B0aW9ucywgY29uc29sZSk7XG4gICAgICAgIH1cbiAgICAgIH1cbiAgICAgIHNlcnZlci53YXRjaGVyLm9uKCdhZGQnLCBoYW5kbGVUaGVtZUZpbGVDcmVhdGVEZWxldGUpO1xuICAgICAgc2VydmVyLndhdGNoZXIub24oJ3VubGluaycsIGhhbmRsZVRoZW1lRmlsZUNyZWF0ZURlbGV0ZSk7XG4gICAgfSxcbiAgICBoYW5kbGVIb3RVcGRhdGUoY29udGV4dCkge1xuICAgICAgY29uc3QgY29udGV4dFBhdGggPSBwYXRoLnJlc29sdmUoY29udGV4dC5maWxlKTtcbiAgICAgIGNvbnN0IHRoZW1lUGF0aCA9IHBhdGgucmVzb2x2ZSh0aGVtZUZvbGRlcik7XG4gICAgICBpZiAoY29udGV4dFBhdGguc3RhcnRzV2l0aCh0aGVtZVBhdGgpKSB7XG4gICAgICAgIGNvbnN0IGNoYW5nZWQgPSBwYXRoLnJlbGF0aXZlKHRoZW1lUGF0aCwgY29udGV4dFBhdGgpO1xuXG4gICAgICAgIGNvbnNvbGUuZGVidWcoJ1RoZW1lIGZpbGUgY2hhbmdlZCcsIGNoYW5nZWQpO1xuXG4gICAgICAgIGlmIChjaGFuZ2VkLnN0YXJ0c1dpdGgoc2V0dGluZ3MudGhlbWVOYW1lKSkge1xuICAgICAgICAgIHByb2Nlc3NUaGVtZVJlc291cmNlcyhmdWxsVGhlbWVPcHRpb25zLCBjb25zb2xlKTtcbiAgICAgICAgfVxuICAgICAgfVxuICAgIH0sXG4gICAgYXN5bmMgcmVzb2x2ZUlkKGlkLCBpbXBvcnRlcikge1xuICAgICAgLy8gZm9yY2UgdGhlbWUgZ2VuZXJhdGlvbiBpZiBnZW5lcmF0ZWQgdGhlbWUgc291cmNlcyBkb2VzIG5vdCB5ZXQgZXhpc3RcbiAgICAgIC8vIHRoaXMgbWF5IGhhcHBlbiBmb3IgZXhhbXBsZSBkdXJpbmcgSmF2YSBob3QgcmVsb2FkIHdoZW4gdXBkYXRpbmdcbiAgICAgIC8vIEBUaGVtZSBhbm5vdGF0aW9uIHZhbHVlXG4gICAgICBpZiAoXG4gICAgICAgIHBhdGgucmVzb2x2ZSh0aGVtZU9wdGlvbnMuZnJvbnRlbmRHZW5lcmF0ZWRGb2xkZXIsICd0aGVtZS5qcycpID09PSBpbXBvcnRlciAmJlxuICAgICAgICAhZXhpc3RzU3luYyhwYXRoLnJlc29sdmUodGhlbWVPcHRpb25zLmZyb250ZW5kR2VuZXJhdGVkRm9sZGVyLCBpZCkpXG4gICAgICApIHtcbiAgICAgICAgY29uc29sZS5kZWJ1ZygnR2VuZXJhdGUgdGhlbWUgZmlsZSAnICsgaWQgKyAnIG5vdCBleGlzdGluZy4gUHJvY2Vzc2luZyB0aGVtZSByZXNvdXJjZScpO1xuICAgICAgICBwcm9jZXNzVGhlbWVSZXNvdXJjZXMoZnVsbFRoZW1lT3B0aW9ucywgY29uc29sZSk7XG4gICAgICAgIHJldHVybjtcbiAgICAgIH1cbiAgICAgIGlmICghaWQuc3RhcnRzV2l0aChzZXR0aW5ncy50aGVtZUZvbGRlcikpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuICAgICAgZm9yIChjb25zdCBsb2NhdGlvbiBvZiBbdGhlbWVSZXNvdXJjZUZvbGRlciwgZnJvbnRlbmRGb2xkZXJdKSB7XG4gICAgICAgIGNvbnN0IHJlc3VsdCA9IGF3YWl0IHRoaXMucmVzb2x2ZShwYXRoLnJlc29sdmUobG9jYXRpb24sIGlkKSk7XG4gICAgICAgIGlmIChyZXN1bHQpIHtcbiAgICAgICAgICByZXR1cm4gcmVzdWx0O1xuICAgICAgICB9XG4gICAgICB9XG4gICAgfSxcbiAgICBhc3luYyB0cmFuc2Zvcm0ocmF3LCBpZCwgb3B0aW9ucykge1xuICAgICAgLy8gcmV3cml0ZSB1cmxzIGZvciB0aGUgYXBwbGljYXRpb24gdGhlbWUgY3NzIGZpbGVzXG4gICAgICBjb25zdCBbYmFyZUlkLCBxdWVyeV0gPSBpZC5zcGxpdCgnPycpO1xuICAgICAgaWYgKFxuICAgICAgICAoIWJhcmVJZD8uc3RhcnRzV2l0aCh0aGVtZUZvbGRlcikgJiYgIWJhcmVJZD8uc3RhcnRzV2l0aCh0aGVtZU9wdGlvbnMudGhlbWVSZXNvdXJjZUZvbGRlcikpIHx8XG4gICAgICAgICFiYXJlSWQ/LmVuZHNXaXRoKCcuY3NzJylcbiAgICAgICkge1xuICAgICAgICByZXR1cm47XG4gICAgICB9XG4gICAgICBjb25zdCByZXNvdXJjZVRoZW1lRm9sZGVyID0gYmFyZUlkLnN0YXJ0c1dpdGgodGhlbWVGb2xkZXIpID8gdGhlbWVGb2xkZXIgOiB0aGVtZU9wdGlvbnMudGhlbWVSZXNvdXJjZUZvbGRlcjtcbiAgICAgIGNvbnN0IFt0aGVtZU5hbWVdID0gIGJhcmVJZC5zdWJzdHJpbmcocmVzb3VyY2VUaGVtZUZvbGRlci5sZW5ndGggKyAxKS5zcGxpdCgnLycpO1xuICAgICAgcmV0dXJuIHJld3JpdGVDc3NVcmxzKHJhdywgcGF0aC5kaXJuYW1lKGJhcmVJZCksIHBhdGgucmVzb2x2ZShyZXNvdXJjZVRoZW1lRm9sZGVyLCB0aGVtZU5hbWUpLCBjb25zb2xlLCBvcHRzKTtcbiAgICB9XG4gIH07XG59XG5cbmZ1bmN0aW9uIHJ1bldhdGNoRG9nKHdhdGNoRG9nUG9ydCwgd2F0Y2hEb2dIb3N0KSB7XG4gIGNvbnN0IGNsaWVudCA9IG5ldC5Tb2NrZXQoKTtcbiAgY2xpZW50LnNldEVuY29kaW5nKCd1dGY4Jyk7XG4gIGNsaWVudC5vbignZXJyb3InLCBmdW5jdGlvbiAoZXJyKSB7XG4gICAgY29uc29sZS5sb2coJ1dhdGNoZG9nIGNvbm5lY3Rpb24gZXJyb3IuIFRlcm1pbmF0aW5nIHZpdGUgcHJvY2Vzcy4uLicsIGVycik7XG4gICAgY2xpZW50LmRlc3Ryb3koKTtcbiAgICBwcm9jZXNzLmV4aXQoMCk7XG4gIH0pO1xuICBjbGllbnQub24oJ2Nsb3NlJywgZnVuY3Rpb24gKCkge1xuICAgIGNsaWVudC5kZXN0cm95KCk7XG4gICAgcnVuV2F0Y2hEb2cod2F0Y2hEb2dQb3J0LCB3YXRjaERvZ0hvc3QpO1xuICB9KTtcblxuICBjbGllbnQuY29ubmVjdCh3YXRjaERvZ1BvcnQsIHdhdGNoRG9nSG9zdCB8fCAnbG9jYWxob3N0Jyk7XG59XG5cbmNvbnN0IGFsbG93ZWRGcm9udGVuZEZvbGRlcnMgPSBbZnJvbnRlbmRGb2xkZXIsIG5vZGVNb2R1bGVzRm9sZGVyXTtcblxuZnVuY3Rpb24gc2hvd1JlY29tcGlsZVJlYXNvbigpOiBQbHVnaW5PcHRpb24ge1xuICByZXR1cm4ge1xuICAgIG5hbWU6ICd2YWFkaW46d2h5LXlvdS1jb21waWxlJyxcbiAgICBoYW5kbGVIb3RVcGRhdGUoY29udGV4dCkge1xuICAgICAgY29uc29sZS5sb2coJ1JlY29tcGlsaW5nIGJlY2F1c2UnLCBjb250ZXh0LmZpbGUsICdjaGFuZ2VkJyk7XG4gICAgfVxuICB9O1xufVxuXG5jb25zdCBERVZfTU9ERV9TVEFSVF9SRUdFWFAgPSAvXFwvXFwqW1xcKiFdXFxzK3ZhYWRpbi1kZXYtbW9kZTpzdGFydC87XG5jb25zdCBERVZfTU9ERV9DT0RFX1JFR0VYUCA9IC9cXC9cXCpbXFwqIV1cXHMrdmFhZGluLWRldi1tb2RlOnN0YXJ0KFtcXHNcXFNdKil2YWFkaW4tZGV2LW1vZGU6ZW5kXFxzK1xcKlxcKlxcLy9pO1xuXG5mdW5jdGlvbiBwcmVzZXJ2ZVVzYWdlU3RhdHMoKSB7XG4gIHJldHVybiB7XG4gICAgbmFtZTogJ3ZhYWRpbjpwcmVzZXJ2ZS11c2FnZS1zdGF0cycsXG5cbiAgICB0cmFuc2Zvcm0oc3JjOiBzdHJpbmcsIGlkOiBzdHJpbmcpIHtcbiAgICAgIGlmIChpZC5pbmNsdWRlcygndmFhZGluLXVzYWdlLXN0YXRpc3RpY3MnKSkge1xuICAgICAgICBpZiAoc3JjLmluY2x1ZGVzKCd2YWFkaW4tZGV2LW1vZGU6c3RhcnQnKSkge1xuICAgICAgICAgIGNvbnN0IG5ld1NyYyA9IHNyYy5yZXBsYWNlKERFVl9NT0RFX1NUQVJUX1JFR0VYUCwgJy8qISB2YWFkaW4tZGV2LW1vZGU6c3RhcnQnKTtcbiAgICAgICAgICBpZiAobmV3U3JjID09PSBzcmMpIHtcbiAgICAgICAgICAgIGNvbnNvbGUuZXJyb3IoJ0NvbW1lbnQgcmVwbGFjZW1lbnQgZmFpbGVkIHRvIGNoYW5nZSBhbnl0aGluZycpO1xuICAgICAgICAgIH0gZWxzZSBpZiAoIW5ld1NyYy5tYXRjaChERVZfTU9ERV9DT0RFX1JFR0VYUCkpIHtcbiAgICAgICAgICAgIGNvbnNvbGUuZXJyb3IoJ05ldyBjb21tZW50IGZhaWxzIHRvIG1hdGNoIG9yaWdpbmFsIHJlZ2V4cCcpO1xuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICByZXR1cm4geyBjb2RlOiBuZXdTcmMgfTtcbiAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICAgIH1cblxuICAgICAgcmV0dXJuIHsgY29kZTogc3JjIH07XG4gICAgfVxuICB9O1xufVxuXG5leHBvcnQgY29uc3QgdmFhZGluQ29uZmlnOiBVc2VyQ29uZmlnRm4gPSAoZW52KSA9PiB7XG4gIGNvbnN0IGRldk1vZGUgPSBlbnYubW9kZSA9PT0gJ2RldmVsb3BtZW50JztcbiAgY29uc3QgcHJvZHVjdGlvbk1vZGUgPSAhZGV2TW9kZSAmJiAhZGV2QnVuZGxlXG5cbiAgaWYgKGRldk1vZGUgJiYgcHJvY2Vzcy5lbnYud2F0Y2hEb2dQb3J0KSB7XG4gICAgLy8gT3BlbiBhIGNvbm5lY3Rpb24gd2l0aCB0aGUgSmF2YSBkZXYtbW9kZSBoYW5kbGVyIGluIG9yZGVyIHRvIGZpbmlzaFxuICAgIC8vIHZpdGUgd2hlbiBpdCBleGl0cyBvciBjcmFzaGVzLlxuICAgIHJ1bldhdGNoRG9nKHByb2Nlc3MuZW52LndhdGNoRG9nUG9ydCwgcHJvY2Vzcy5lbnYud2F0Y2hEb2dIb3N0KTtcbiAgfVxuXG4gIHJldHVybiB7XG4gICAgcm9vdDogZnJvbnRlbmRGb2xkZXIsXG4gICAgYmFzZTogJycsXG4gICAgcHVibGljRGlyOiBmYWxzZSxcbiAgICByZXNvbHZlOiB7XG4gICAgICBhbGlhczoge1xuICAgICAgICAnQHZhYWRpbi9mbG93LWZyb250ZW5kJzogamFyUmVzb3VyY2VzRm9sZGVyLFxuICAgICAgICBGcm9udGVuZDogZnJvbnRlbmRGb2xkZXJcbiAgICAgIH0sXG4gICAgICBwcmVzZXJ2ZVN5bWxpbmtzOiB0cnVlXG4gICAgfSxcbiAgICBkZWZpbmU6IHtcbiAgICAgIE9GRkxJTkVfUEFUSDogc2V0dGluZ3Mub2ZmbGluZVBhdGgsXG4gICAgICBWSVRFX0VOQUJMRUQ6ICd0cnVlJ1xuICAgIH0sXG4gICAgc2VydmVyOiB7XG4gICAgICBob3N0OiAnMTI3LjAuMC4xJyxcbiAgICAgIHN0cmljdFBvcnQ6IHRydWUsXG4gICAgICBmczoge1xuICAgICAgICBhbGxvdzogYWxsb3dlZEZyb250ZW5kRm9sZGVyc1xuICAgICAgfVxuICAgIH0sXG4gICAgYnVpbGQ6IHtcbiAgICAgIG1pbmlmeTogcHJvZHVjdGlvbk1vZGUsXG4gICAgICBvdXREaXI6IGJ1aWxkT3V0cHV0Rm9sZGVyLFxuICAgICAgZW1wdHlPdXREaXI6IGRldkJ1bmRsZSxcbiAgICAgIGFzc2V0c0RpcjogJ1ZBQURJTi9idWlsZCcsXG4gICAgICB0YXJnZXQ6IFtcImVzbmV4dFwiLCBcInNhZmFyaTE1XCJdLFxuICAgICAgcm9sbHVwT3B0aW9uczoge1xuICAgICAgICBpbnB1dDoge1xuICAgICAgICAgIGluZGV4aHRtbDogcHJvamVjdEluZGV4SHRtbCxcblxuICAgICAgICAgIC4uLihoYXNFeHBvcnRlZFdlYkNvbXBvbmVudHMgPyB7IHdlYmNvbXBvbmVudGh0bWw6IHBhdGgucmVzb2x2ZShmcm9udGVuZEZvbGRlciwgJ3dlYi1jb21wb25lbnQuaHRtbCcpIH0gOiB7fSlcbiAgICAgICAgfSxcbiAgICAgICAgb253YXJuOiAod2FybmluZzogcm9sbHVwLlJvbGx1cFdhcm5pbmcsIGRlZmF1bHRIYW5kbGVyOiByb2xsdXAuV2FybmluZ0hhbmRsZXIpID0+IHtcbiAgICAgICAgICBjb25zdCBpZ25vcmVFdmFsV2FybmluZyA9IFtcbiAgICAgICAgICAgICdnZW5lcmF0ZWQvamFyLXJlc291cmNlcy9GbG93Q2xpZW50LmpzJyxcbiAgICAgICAgICAgICdnZW5lcmF0ZWQvamFyLXJlc291cmNlcy92YWFkaW4tc3ByZWFkc2hlZXQvc3ByZWFkc2hlZXQtZXhwb3J0LmpzJyxcbiAgICAgICAgICAgICdAdmFhZGluL2NoYXJ0cy9zcmMvaGVscGVycy5qcydcbiAgICAgICAgICBdO1xuICAgICAgICAgIGlmICh3YXJuaW5nLmNvZGUgPT09ICdFVkFMJyAmJiB3YXJuaW5nLmlkICYmICEhaWdub3JlRXZhbFdhcm5pbmcuZmluZCgoaWQpID0+IHdhcm5pbmcuaWQuZW5kc1dpdGgoaWQpKSkge1xuICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICAgIH1cbiAgICAgICAgICBkZWZhdWx0SGFuZGxlcih3YXJuaW5nKTtcbiAgICAgICAgfVxuICAgICAgfVxuICAgIH0sXG4gICAgb3B0aW1pemVEZXBzOiB7XG4gICAgICBlbnRyaWVzOiBbXG4gICAgICAgIC8vIFByZS1zY2FuIGVudHJ5cG9pbnRzIGluIFZpdGUgdG8gYXZvaWQgcmVsb2FkaW5nIG9uIGZpcnN0IG9wZW5cbiAgICAgICAgJ2dlbmVyYXRlZC92YWFkaW4udHMnXG4gICAgICBdLFxuICAgICAgZXhjbHVkZTogW1xuICAgICAgICAnQHZhYWRpbi9yb3V0ZXInLFxuICAgICAgICAnQHZhYWRpbi92YWFkaW4tbGljZW5zZS1jaGVja2VyJyxcbiAgICAgICAgJ0B2YWFkaW4vdmFhZGluLXVzYWdlLXN0YXRpc3RpY3MnLFxuICAgICAgICAnd29ya2JveC1jb3JlJyxcbiAgICAgICAgJ3dvcmtib3gtcHJlY2FjaGluZycsXG4gICAgICAgICd3b3JrYm94LXJvdXRpbmcnLFxuICAgICAgICAnd29ya2JveC1zdHJhdGVnaWVzJ1xuICAgICAgXVxuICAgIH0sXG4gICAgcGx1Z2luczogW1xuICAgICAgcHJvZHVjdGlvbk1vZGUgJiYgYnJvdGxpKCksXG4gICAgICBkZXZNb2RlICYmIHZhYWRpbkJ1bmRsZXNQbHVnaW4oKSxcbiAgICAgIGRldk1vZGUgJiYgc2hvd1JlY29tcGlsZVJlYXNvbigpLFxuICAgICAgc2V0dGluZ3Mub2ZmbGluZUVuYWJsZWQgJiYgYnVpbGRTV1BsdWdpbih7IGRldk1vZGUgfSksXG4gICAgICAhZGV2TW9kZSAmJiBzdGF0c0V4dHJhY3RlclBsdWdpbigpLFxuICAgICAgZGV2QnVuZGxlICYmIHByZXNlcnZlVXNhZ2VTdGF0cygpLFxuICAgICAgdGhlbWVQbHVnaW4oeyBkZXZNb2RlIH0pLFxuICAgICAgcG9zdGNzc0xpdCh7XG4gICAgICAgIGluY2x1ZGU6IFsnKiovKi5jc3MnLCAvLipcXC8uKlxcLmNzc1xcPy4qL10sXG4gICAgICAgIGV4Y2x1ZGU6IFtcbiAgICAgICAgICBgJHt0aGVtZUZvbGRlcn0vKiovKi5jc3NgLFxuICAgICAgICAgIG5ldyBSZWdFeHAoYCR7dGhlbWVGb2xkZXJ9Ly4qLy4qXFxcXC5jc3NcXFxcPy4qYCksXG4gICAgICAgICAgYCR7dGhlbWVSZXNvdXJjZUZvbGRlcn0vKiovKi5jc3NgLFxuICAgICAgICAgIG5ldyBSZWdFeHAoYCR7dGhlbWVSZXNvdXJjZUZvbGRlcn0vLiovLipcXFxcLmNzc1xcXFw/LipgKSxcbiAgICAgICAgICBuZXcgUmVnRXhwKCcuKi8uKlxcXFw/aHRtbC1wcm94eS4qJylcbiAgICAgICAgXVxuICAgICAgfSksXG4gICAgICAvLyBUaGUgUmVhY3QgcGx1Z2luIHByb3ZpZGVzIGZhc3QgcmVmcmVzaCBhbmQgZGVidWcgc291cmNlIGluZm9cbiAgICAgIHJlYWN0UGx1Z2luKHtcbiAgICAgICAgaW5jbHVkZTogJyoqLyoudHN4JyxcbiAgICAgICAgYmFiZWw6IHtcbiAgICAgICAgICAvLyBXZSBuZWVkIHRvIHVzZSBiYWJlbCB0byBwcm92aWRlIHRoZSBzb3VyY2UgaW5mb3JtYXRpb24gZm9yIGl0IHRvIGJlIGNvcnJlY3RcbiAgICAgICAgICAvLyAob3RoZXJ3aXNlIEJhYmVsIHdpbGwgc2xpZ2h0bHkgcmV3cml0ZSB0aGUgc291cmNlIGZpbGUgYW5kIGVzYnVpbGQgZ2VuZXJhdGUgc291cmNlIGluZm8gZm9yIHRoZSBtb2RpZmllZCBmaWxlKVxuICAgICAgICAgIHByZXNldHM6IFtbJ0BiYWJlbC9wcmVzZXQtcmVhY3QnLCB7IHJ1bnRpbWU6ICdhdXRvbWF0aWMnLCBkZXZlbG9wbWVudDogIXByb2R1Y3Rpb25Nb2RlIH1dXSxcbiAgICAgICAgICAvLyBSZWFjdCB3cml0ZXMgdGhlIHNvdXJjZSBsb2NhdGlvbiBmb3Igd2hlcmUgY29tcG9uZW50cyBhcmUgdXNlZCwgdGhpcyB3cml0ZXMgZm9yIHdoZXJlIHRoZXkgYXJlIGRlZmluZWRcbiAgICAgICAgICBwbHVnaW5zOiBbXG4gICAgICAgICAgICAhcHJvZHVjdGlvbk1vZGUgJiYgYWRkRnVuY3Rpb25Db21wb25lbnRTb3VyY2VMb2NhdGlvbkJhYmVsKClcbiAgICAgICAgICBdLmZpbHRlcihCb29sZWFuKVxuICAgICAgICB9XG4gICAgICB9KSxcbiAgICAgIHtcbiAgICAgICAgbmFtZTogJ3ZhYWRpbjpmb3JjZS1yZW1vdmUtaHRtbC1taWRkbGV3YXJlJyxcbiAgICAgICAgY29uZmlndXJlU2VydmVyKHNlcnZlcikge1xuICAgICAgICAgIHJldHVybiAoKSA9PiB7XG4gICAgICAgICAgICBzZXJ2ZXIubWlkZGxld2FyZXMuc3RhY2sgPSBzZXJ2ZXIubWlkZGxld2FyZXMuc3RhY2suZmlsdGVyKChtdykgPT4ge1xuICAgICAgICAgICAgICBjb25zdCBoYW5kbGVOYW1lID0gYCR7bXcuaGFuZGxlfWA7XG4gICAgICAgICAgICAgIHJldHVybiAhaGFuZGxlTmFtZS5pbmNsdWRlcygndml0ZUh0bWxGYWxsYmFja01pZGRsZXdhcmUnKTtcbiAgICAgICAgICAgIH0pO1xuICAgICAgICAgIH07XG4gICAgICAgIH0sXG4gICAgICB9LFxuICAgICAgaGFzRXhwb3J0ZWRXZWJDb21wb25lbnRzICYmIHtcbiAgICAgICAgbmFtZTogJ3ZhYWRpbjppbmplY3QtZW50cnlwb2ludHMtdG8td2ViLWNvbXBvbmVudC1odG1sJyxcbiAgICAgICAgdHJhbnNmb3JtSW5kZXhIdG1sOiB7XG4gICAgICAgICAgb3JkZXI6ICdwcmUnLFxuICAgICAgICAgIGhhbmRsZXIoX2h0bWwsIHsgcGF0aCwgc2VydmVyIH0pIHtcbiAgICAgICAgICAgIGlmIChwYXRoICE9PSAnL3dlYi1jb21wb25lbnQuaHRtbCcpIHtcbiAgICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICByZXR1cm4gW1xuICAgICAgICAgICAgICB7XG4gICAgICAgICAgICAgICAgdGFnOiAnc2NyaXB0JyxcbiAgICAgICAgICAgICAgICBhdHRyczogeyB0eXBlOiAnbW9kdWxlJywgc3JjOiBgL2dlbmVyYXRlZC92YWFkaW4td2ViLWNvbXBvbmVudC50c2AgfSxcbiAgICAgICAgICAgICAgICBpbmplY3RUbzogJ2hlYWQnXG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIF07XG4gICAgICAgICAgfVxuICAgICAgICB9XG4gICAgICB9LFxuICAgICAge1xuICAgICAgICBuYW1lOiAndmFhZGluOmluamVjdC1lbnRyeXBvaW50cy10by1pbmRleC1odG1sJyxcbiAgICAgICAgdHJhbnNmb3JtSW5kZXhIdG1sOiB7XG4gICAgICAgICAgb3JkZXI6ICdwcmUnLFxuICAgICAgICAgIGhhbmRsZXIoX2h0bWwsIHsgcGF0aCwgc2VydmVyIH0pIHtcbiAgICAgICAgICAgIGlmIChwYXRoICE9PSAnL2luZGV4Lmh0bWwnKSB7XG4gICAgICAgICAgICAgIHJldHVybjtcbiAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgY29uc3Qgc2NyaXB0cyA9IFtdO1xuXG4gICAgICAgICAgICBpZiAoZGV2TW9kZSkge1xuICAgICAgICAgICAgICBzY3JpcHRzLnB1c2goe1xuICAgICAgICAgICAgICAgIHRhZzogJ3NjcmlwdCcsXG4gICAgICAgICAgICAgICAgYXR0cnM6IHsgdHlwZTogJ21vZHVsZScsIHNyYzogYC9nZW5lcmF0ZWQvdml0ZS1kZXZtb2RlLnRzYCB9LFxuICAgICAgICAgICAgICAgIGluamVjdFRvOiAnaGVhZCdcbiAgICAgICAgICAgICAgfSk7XG4gICAgICAgICAgICB9XG4gICAgICAgICAgICBzY3JpcHRzLnB1c2goe1xuICAgICAgICAgICAgICB0YWc6ICdzY3JpcHQnLFxuICAgICAgICAgICAgICBhdHRyczogeyB0eXBlOiAnbW9kdWxlJywgc3JjOiAnL2dlbmVyYXRlZC92YWFkaW4udHMnIH0sXG4gICAgICAgICAgICAgIGluamVjdFRvOiAnaGVhZCdcbiAgICAgICAgICAgIH0pO1xuICAgICAgICAgICAgcmV0dXJuIHNjcmlwdHM7XG4gICAgICAgICAgfVxuICAgICAgICB9XG4gICAgICB9LFxuICAgICAgY2hlY2tlcih7XG4gICAgICAgIHR5cGVzY3JpcHQ6IHRydWVcbiAgICAgIH0pLFxuICAgICAgcHJvZHVjdGlvbk1vZGUgJiYgdmlzdWFsaXplcih7IGJyb3RsaVNpemU6IHRydWUsIGZpbGVuYW1lOiBidW5kbGVTaXplRmlsZSB9KVxuICAgICAgXG4gICAgXVxuICB9O1xufTtcblxuZXhwb3J0IGNvbnN0IG92ZXJyaWRlVmFhZGluQ29uZmlnID0gKGN1c3RvbUNvbmZpZzogVXNlckNvbmZpZ0ZuKSA9PiB7XG4gIHJldHVybiBkZWZpbmVDb25maWcoKGVudikgPT4gbWVyZ2VDb25maWcodmFhZGluQ29uZmlnKGVudiksIGN1c3RvbUNvbmZpZyhlbnYpKSk7XG59O1xuZnVuY3Rpb24gZ2V0VmVyc2lvbihtb2R1bGU6IHN0cmluZyk6IHN0cmluZyB7XG4gIGNvbnN0IHBhY2thZ2VKc29uID0gcGF0aC5yZXNvbHZlKG5vZGVNb2R1bGVzRm9sZGVyLCBtb2R1bGUsICdwYWNrYWdlLmpzb24nKTtcbiAgcmV0dXJuIEpTT04ucGFyc2UocmVhZEZpbGVTeW5jKHBhY2thZ2VKc29uLCB7IGVuY29kaW5nOiAndXRmLTgnIH0pKS52ZXJzaW9uO1xufVxuZnVuY3Rpb24gZ2V0Q3ZkbE5hbWUobW9kdWxlOiBzdHJpbmcpOiBzdHJpbmcge1xuICBjb25zdCBwYWNrYWdlSnNvbiA9IHBhdGgucmVzb2x2ZShub2RlTW9kdWxlc0ZvbGRlciwgbW9kdWxlLCAncGFja2FnZS5qc29uJyk7XG4gIHJldHVybiBKU09OLnBhcnNlKHJlYWRGaWxlU3luYyhwYWNrYWdlSnNvbiwgeyBlbmNvZGluZzogJ3V0Zi04JyB9KSkuY3ZkbE5hbWU7XG59XG4iLCAiY29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2Rpcm5hbWUgPSBcIkY6XFxcXFByb3llY3RvLVNlZ3VyaWRhZFxcXFxnZXN0aW9uLXNlZ3VyaWRhZC1hZXJvcG9ydHVhcmlhXFxcXHRhcmdldFxcXFxwbHVnaW5zXFxcXGFwcGxpY2F0aW9uLXRoZW1lLXBsdWdpblwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiRjpcXFxcUHJveWVjdG8tU2VndXJpZGFkXFxcXGdlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWFcXFxcdGFyZ2V0XFxcXHBsdWdpbnNcXFxcYXBwbGljYXRpb24tdGhlbWUtcGx1Z2luXFxcXHRoZW1lLWhhbmRsZS5qc1wiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9pbXBvcnRfbWV0YV91cmwgPSBcImZpbGU6Ly8vRjovUHJveWVjdG8tU2VndXJpZGFkL2dlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWEvdGFyZ2V0L3BsdWdpbnMvYXBwbGljYXRpb24tdGhlbWUtcGx1Z2luL3RoZW1lLWhhbmRsZS5qc1wiOy8qXG4gKiBDb3B5cmlnaHQgMjAwMC0yMDI0IFZhYWRpbiBMdGQuXG4gKlxuICogTGljZW5zZWQgdW5kZXIgdGhlIEFwYWNoZSBMaWNlbnNlLCBWZXJzaW9uIDIuMCAodGhlIFwiTGljZW5zZVwiKTsgeW91IG1heSBub3RcbiAqIHVzZSB0aGlzIGZpbGUgZXhjZXB0IGluIGNvbXBsaWFuY2Ugd2l0aCB0aGUgTGljZW5zZS4gWW91IG1heSBvYnRhaW4gYSBjb3B5IG9mXG4gKiB0aGUgTGljZW5zZSBhdFxuICpcbiAqIGh0dHA6Ly93d3cuYXBhY2hlLm9yZy9saWNlbnNlcy9MSUNFTlNFLTIuMFxuICpcbiAqIFVubGVzcyByZXF1aXJlZCBieSBhcHBsaWNhYmxlIGxhdyBvciBhZ3JlZWQgdG8gaW4gd3JpdGluZywgc29mdHdhcmVcbiAqIGRpc3RyaWJ1dGVkIHVuZGVyIHRoZSBMaWNlbnNlIGlzIGRpc3RyaWJ1dGVkIG9uIGFuIFwiQVMgSVNcIiBCQVNJUywgV0lUSE9VVFxuICogV0FSUkFOVElFUyBPUiBDT05ESVRJT05TIE9GIEFOWSBLSU5ELCBlaXRoZXIgZXhwcmVzcyBvciBpbXBsaWVkLiBTZWUgdGhlXG4gKiBMaWNlbnNlIGZvciB0aGUgc3BlY2lmaWMgbGFuZ3VhZ2UgZ292ZXJuaW5nIHBlcm1pc3Npb25zIGFuZCBsaW1pdGF0aW9ucyB1bmRlclxuICogdGhlIExpY2Vuc2UuXG4gKi9cblxuLyoqXG4gKiBUaGlzIGZpbGUgY29udGFpbnMgZnVuY3Rpb25zIGZvciBsb29rIHVwIGFuZCBoYW5kbGUgdGhlIHRoZW1lIHJlc291cmNlc1xuICogZm9yIGFwcGxpY2F0aW9uIHRoZW1lIHBsdWdpbi5cbiAqL1xuaW1wb3J0IHsgZXhpc3RzU3luYywgcmVhZEZpbGVTeW5jIH0gZnJvbSAnZnMnO1xuaW1wb3J0IHsgcmVzb2x2ZSB9IGZyb20gJ3BhdGgnO1xuaW1wb3J0IHsgd3JpdGVUaGVtZUZpbGVzIH0gZnJvbSAnLi90aGVtZS1nZW5lcmF0b3IuanMnO1xuaW1wb3J0IHsgY29weVN0YXRpY0Fzc2V0cywgY29weVRoZW1lUmVzb3VyY2VzIH0gZnJvbSAnLi90aGVtZS1jb3B5LmpzJztcblxuLy8gbWF0Y2hlcyB0aGVtZSBuYW1lIGluICcuL3RoZW1lLW15LXRoZW1lLmdlbmVyYXRlZC5qcydcbmNvbnN0IG5hbWVSZWdleCA9IC90aGVtZS0oLiopXFwuZ2VuZXJhdGVkXFwuanMvO1xuXG5sZXQgcHJldlRoZW1lTmFtZSA9IHVuZGVmaW5lZDtcbmxldCBmaXJzdFRoZW1lTmFtZSA9IHVuZGVmaW5lZDtcblxuLyoqXG4gKiBMb29rcyB1cCBmb3IgYSB0aGVtZSByZXNvdXJjZXMgaW4gYSBjdXJyZW50IHByb2plY3QgYW5kIGluIGphciBkZXBlbmRlbmNpZXMsXG4gKiBjb3BpZXMgdGhlIGZvdW5kIHJlc291cmNlcyBhbmQgZ2VuZXJhdGVzL3VwZGF0ZXMgbWV0YSBkYXRhIGZvciB3ZWJwYWNrXG4gKiBjb21waWxhdGlvbi5cbiAqXG4gKiBAcGFyYW0ge29iamVjdH0gb3B0aW9ucyBhcHBsaWNhdGlvbiB0aGVtZSBwbHVnaW4gbWFuZGF0b3J5IG9wdGlvbnMsXG4gKiBAc2VlIHtAbGluayBBcHBsaWNhdGlvblRoZW1lUGx1Z2lufVxuICpcbiAqIEBwYXJhbSBsb2dnZXIgYXBwbGljYXRpb24gdGhlbWUgcGx1Z2luIGxvZ2dlclxuICovXG5mdW5jdGlvbiBwcm9jZXNzVGhlbWVSZXNvdXJjZXMob3B0aW9ucywgbG9nZ2VyKSB7XG4gIGNvbnN0IHRoZW1lTmFtZSA9IGV4dHJhY3RUaGVtZU5hbWUob3B0aW9ucy5mcm9udGVuZEdlbmVyYXRlZEZvbGRlcik7XG4gIGlmICh0aGVtZU5hbWUpIHtcbiAgICBpZiAoIXByZXZUaGVtZU5hbWUgJiYgIWZpcnN0VGhlbWVOYW1lKSB7XG4gICAgICBmaXJzdFRoZW1lTmFtZSA9IHRoZW1lTmFtZTtcbiAgICB9IGVsc2UgaWYgKFxuICAgICAgKHByZXZUaGVtZU5hbWUgJiYgcHJldlRoZW1lTmFtZSAhPT0gdGhlbWVOYW1lICYmIGZpcnN0VGhlbWVOYW1lICE9PSB0aGVtZU5hbWUpIHx8XG4gICAgICAoIXByZXZUaGVtZU5hbWUgJiYgZmlyc3RUaGVtZU5hbWUgIT09IHRoZW1lTmFtZSlcbiAgICApIHtcbiAgICAgIC8vIFdhcm5pbmcgbWVzc2FnZSBpcyBzaG93biB0byB0aGUgZGV2ZWxvcGVyIHdoZW46XG4gICAgICAvLyAxLiBIZSBpcyBzd2l0Y2hpbmcgdG8gYW55IHRoZW1lLCB3aGljaCBpcyBkaWZmZXIgZnJvbSBvbmUgYmVpbmcgc2V0IHVwXG4gICAgICAvLyBvbiBhcHBsaWNhdGlvbiBzdGFydHVwLCBieSBjaGFuZ2luZyB0aGVtZSBuYW1lIGluIGBAVGhlbWUoKWBcbiAgICAgIC8vIDIuIEhlIHJlbW92ZXMgb3IgY29tbWVudHMgb3V0IGBAVGhlbWUoKWAgdG8gc2VlIGhvdyB0aGUgYXBwXG4gICAgICAvLyBsb29rcyBsaWtlIHdpdGhvdXQgdGhlbWluZywgYW5kIHRoZW4gYWdhaW4gYnJpbmdzIGBAVGhlbWUoKWAgYmFja1xuICAgICAgLy8gd2l0aCBhIHRoZW1lTmFtZSB3aGljaCBpcyBkaWZmZXIgZnJvbSBvbmUgYmVpbmcgc2V0IHVwIG9uIGFwcGxpY2F0aW9uXG4gICAgICAvLyBzdGFydHVwLlxuICAgICAgY29uc3Qgd2FybmluZyA9IGBBdHRlbnRpb246IEFjdGl2ZSB0aGVtZSBpcyBzd2l0Y2hlZCB0byAnJHt0aGVtZU5hbWV9Jy5gO1xuICAgICAgY29uc3QgZGVzY3JpcHRpb24gPSBgXG4gICAgICBOb3RlIHRoYXQgYWRkaW5nIG5ldyBzdHlsZSBzaGVldCBmaWxlcyB0byAnL3RoZW1lcy8ke3RoZW1lTmFtZX0vY29tcG9uZW50cycsIFxuICAgICAgbWF5IG5vdCBiZSB0YWtlbiBpbnRvIGVmZmVjdCB1bnRpbCB0aGUgbmV4dCBhcHBsaWNhdGlvbiByZXN0YXJ0LlxuICAgICAgQ2hhbmdlcyB0byBhbHJlYWR5IGV4aXN0aW5nIHN0eWxlIHNoZWV0IGZpbGVzIGFyZSBiZWluZyByZWxvYWRlZCBhcyBiZWZvcmUuYDtcbiAgICAgIGxvZ2dlci53YXJuKCcqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqJyk7XG4gICAgICBsb2dnZXIud2Fybih3YXJuaW5nKTtcbiAgICAgIGxvZ2dlci53YXJuKGRlc2NyaXB0aW9uKTtcbiAgICAgIGxvZ2dlci53YXJuKCcqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqJyk7XG4gICAgfVxuICAgIHByZXZUaGVtZU5hbWUgPSB0aGVtZU5hbWU7XG5cbiAgICBmaW5kVGhlbWVGb2xkZXJBbmRIYW5kbGVUaGVtZSh0aGVtZU5hbWUsIG9wdGlvbnMsIGxvZ2dlcik7XG4gIH0gZWxzZSB7XG4gICAgLy8gVGhpcyBpcyBuZWVkZWQgaW4gdGhlIHNpdHVhdGlvbiB0aGF0IHRoZSB1c2VyIGRlY2lkZXMgdG8gY29tbWVudCBvclxuICAgIC8vIHJlbW92ZSB0aGUgQFRoZW1lKC4uLikgY29tcGxldGVseSB0byBzZWUgaG93IHRoZSBhcHBsaWNhdGlvbiBsb29rc1xuICAgIC8vIHdpdGhvdXQgYW55IHRoZW1lLiBUaGVuIHdoZW4gdGhlIHVzZXIgYnJpbmdzIGJhY2sgb25lIG9mIHRoZSB0aGVtZXMsXG4gICAgLy8gdGhlIHByZXZpb3VzIHRoZW1lIHNob3VsZCBiZSB1bmRlZmluZWQgdG8gZW5hYmxlIHVzIHRvIGRldGVjdCB0aGUgY2hhbmdlLlxuICAgIHByZXZUaGVtZU5hbWUgPSB1bmRlZmluZWQ7XG4gICAgbG9nZ2VyLmRlYnVnKCdTa2lwcGluZyBWYWFkaW4gYXBwbGljYXRpb24gdGhlbWUgaGFuZGxpbmcuJyk7XG4gICAgbG9nZ2VyLnRyYWNlKCdNb3N0IGxpa2VseSBubyBAVGhlbWUgYW5ub3RhdGlvbiBmb3IgYXBwbGljYXRpb24gb3Igb25seSB0aGVtZUNsYXNzIHVzZWQuJyk7XG4gIH1cbn1cblxuLyoqXG4gKiBTZWFyY2ggZm9yIHRoZSBnaXZlbiB0aGVtZSBpbiB0aGUgcHJvamVjdCBhbmQgcmVzb3VyY2UgZm9sZGVycy5cbiAqXG4gKiBAcGFyYW0ge3N0cmluZ30gdGhlbWVOYW1lIG5hbWUgb2YgdGhlbWUgdG8gZmluZFxuICogQHBhcmFtIHtvYmplY3R9IG9wdGlvbnMgYXBwbGljYXRpb24gdGhlbWUgcGx1Z2luIG1hbmRhdG9yeSBvcHRpb25zLFxuICogQHNlZSB7QGxpbmsgQXBwbGljYXRpb25UaGVtZVBsdWdpbn1cbiAqIEBwYXJhbSBsb2dnZXIgYXBwbGljYXRpb24gdGhlbWUgcGx1Z2luIGxvZ2dlclxuICogQHJldHVybiB0cnVlIG9yIGZhbHNlIGZvciBpZiB0aGVtZSB3YXMgZm91bmRcbiAqL1xuZnVuY3Rpb24gZmluZFRoZW1lRm9sZGVyQW5kSGFuZGxlVGhlbWUodGhlbWVOYW1lLCBvcHRpb25zLCBsb2dnZXIpIHtcbiAgbGV0IHRoZW1lRm91bmQgPSBmYWxzZTtcbiAgZm9yIChsZXQgaSA9IDA7IGkgPCBvcHRpb25zLnRoZW1lUHJvamVjdEZvbGRlcnMubGVuZ3RoOyBpKyspIHtcbiAgICBjb25zdCB0aGVtZVByb2plY3RGb2xkZXIgPSBvcHRpb25zLnRoZW1lUHJvamVjdEZvbGRlcnNbaV07XG4gICAgaWYgKGV4aXN0c1N5bmModGhlbWVQcm9qZWN0Rm9sZGVyKSkge1xuICAgICAgbG9nZ2VyLmRlYnVnKFwiU2VhcmNoaW5nIHRoZW1lcyBmb2xkZXIgJ1wiICsgdGhlbWVQcm9qZWN0Rm9sZGVyICsgXCInIGZvciB0aGVtZSAnXCIgKyB0aGVtZU5hbWUgKyBcIidcIik7XG4gICAgICBjb25zdCBoYW5kbGVkID0gaGFuZGxlVGhlbWVzKHRoZW1lTmFtZSwgdGhlbWVQcm9qZWN0Rm9sZGVyLCBvcHRpb25zLCBsb2dnZXIpO1xuICAgICAgaWYgKGhhbmRsZWQpIHtcbiAgICAgICAgaWYgKHRoZW1lRm91bmQpIHtcbiAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoXG4gICAgICAgICAgICBcIkZvdW5kIHRoZW1lIGZpbGVzIGluICdcIiArXG4gICAgICAgICAgICAgIHRoZW1lUHJvamVjdEZvbGRlciArXG4gICAgICAgICAgICAgIFwiJyBhbmQgJ1wiICtcbiAgICAgICAgICAgICAgdGhlbWVGb3VuZCArXG4gICAgICAgICAgICAgIFwiJy4gVGhlbWUgc2hvdWxkIG9ubHkgYmUgYXZhaWxhYmxlIGluIG9uZSBmb2xkZXJcIlxuICAgICAgICAgICk7XG4gICAgICAgIH1cbiAgICAgICAgbG9nZ2VyLmRlYnVnKFwiRm91bmQgdGhlbWUgZmlsZXMgZnJvbSAnXCIgKyB0aGVtZVByb2plY3RGb2xkZXIgKyBcIidcIik7XG4gICAgICAgIHRoZW1lRm91bmQgPSB0aGVtZVByb2plY3RGb2xkZXI7XG4gICAgICB9XG4gICAgfVxuICB9XG5cbiAgaWYgKGV4aXN0c1N5bmMob3B0aW9ucy50aGVtZVJlc291cmNlRm9sZGVyKSkge1xuICAgIGlmICh0aGVtZUZvdW5kICYmIGV4aXN0c1N5bmMocmVzb2x2ZShvcHRpb25zLnRoZW1lUmVzb3VyY2VGb2xkZXIsIHRoZW1lTmFtZSkpKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoXG4gICAgICAgIFwiVGhlbWUgJ1wiICtcbiAgICAgICAgICB0aGVtZU5hbWUgK1xuICAgICAgICAgIFwiJ3Nob3VsZCBub3QgZXhpc3QgaW5zaWRlIGEgamFyIGFuZCBpbiB0aGUgcHJvamVjdCBhdCB0aGUgc2FtZSB0aW1lXFxuXCIgK1xuICAgICAgICAgICdFeHRlbmRpbmcgYW5vdGhlciB0aGVtZSBpcyBwb3NzaWJsZSBieSBhZGRpbmcgeyBcInBhcmVudFwiOiBcIm15LXBhcmVudC10aGVtZVwiIH0gZW50cnkgdG8gdGhlIHRoZW1lLmpzb24gZmlsZSBpbnNpZGUgeW91ciB0aGVtZSBmb2xkZXIuJ1xuICAgICAgKTtcbiAgICB9XG4gICAgbG9nZ2VyLmRlYnVnKFxuICAgICAgXCJTZWFyY2hpbmcgdGhlbWUgamFyIHJlc291cmNlIGZvbGRlciAnXCIgKyBvcHRpb25zLnRoZW1lUmVzb3VyY2VGb2xkZXIgKyBcIicgZm9yIHRoZW1lICdcIiArIHRoZW1lTmFtZSArIFwiJ1wiXG4gICAgKTtcbiAgICBoYW5kbGVUaGVtZXModGhlbWVOYW1lLCBvcHRpb25zLnRoZW1lUmVzb3VyY2VGb2xkZXIsIG9wdGlvbnMsIGxvZ2dlcik7XG4gICAgdGhlbWVGb3VuZCA9IHRydWU7XG4gIH1cbiAgcmV0dXJuIHRoZW1lRm91bmQ7XG59XG5cbi8qKlxuICogQ29waWVzIHN0YXRpYyByZXNvdXJjZXMgZm9yIHRoZW1lIGFuZCBnZW5lcmF0ZXMvd3JpdGVzIHRoZVxuICogW3RoZW1lLW5hbWVdLmdlbmVyYXRlZC5qcyBmb3Igd2VicGFjayB0byBoYW5kbGUuXG4gKlxuICogTm90ZSEgSWYgYSBwYXJlbnQgdGhlbWUgaXMgZGVmaW5lZCBpdCB3aWxsIGFsc28gYmUgaGFuZGxlZCBoZXJlIHNvIHRoYXQgdGhlIHBhcmVudCB0aGVtZSBnZW5lcmF0ZWQgZmlsZSBpc1xuICogZ2VuZXJhdGVkIGluIGFkdmFuY2Ugb2YgdGhlIHRoZW1lIGdlbmVyYXRlZCBmaWxlLlxuICpcbiAqIEBwYXJhbSB7c3RyaW5nfSB0aGVtZU5hbWUgbmFtZSBvZiB0aGVtZSB0byBoYW5kbGVcbiAqIEBwYXJhbSB7c3RyaW5nfSB0aGVtZXNGb2xkZXIgZm9sZGVyIGNvbnRhaW5pbmcgYXBwbGljYXRpb24gdGhlbWUgZm9sZGVyc1xuICogQHBhcmFtIHtvYmplY3R9IG9wdGlvbnMgYXBwbGljYXRpb24gdGhlbWUgcGx1Z2luIG1hbmRhdG9yeSBvcHRpb25zLFxuICogQHNlZSB7QGxpbmsgQXBwbGljYXRpb25UaGVtZVBsdWdpbn1cbiAqIEBwYXJhbSB7b2JqZWN0fSBsb2dnZXIgcGx1Z2luIGxvZ2dlciBpbnN0YW5jZVxuICpcbiAqIEB0aHJvd3MgRXJyb3IgaWYgcGFyZW50IHRoZW1lIGRlZmluZWQsIGJ1dCBjYW4ndCBsb2NhdGUgcGFyZW50IHRoZW1lXG4gKlxuICogQHJldHVybnMgdHJ1ZSBpZiB0aGVtZSB3YXMgZm91bmQgZWxzZSBmYWxzZS5cbiAqL1xuZnVuY3Rpb24gaGFuZGxlVGhlbWVzKHRoZW1lTmFtZSwgdGhlbWVzRm9sZGVyLCBvcHRpb25zLCBsb2dnZXIpIHtcbiAgY29uc3QgdGhlbWVGb2xkZXIgPSByZXNvbHZlKHRoZW1lc0ZvbGRlciwgdGhlbWVOYW1lKTtcbiAgaWYgKGV4aXN0c1N5bmModGhlbWVGb2xkZXIpKSB7XG4gICAgbG9nZ2VyLmRlYnVnKCdGb3VuZCB0aGVtZSAnLCB0aGVtZU5hbWUsICcgaW4gZm9sZGVyICcsIHRoZW1lRm9sZGVyKTtcblxuICAgIGNvbnN0IHRoZW1lUHJvcGVydGllcyA9IGdldFRoZW1lUHJvcGVydGllcyh0aGVtZUZvbGRlcik7XG5cbiAgICAvLyBJZiB0aGVtZSBoYXMgcGFyZW50IGhhbmRsZSBwYXJlbnQgdGhlbWUgaW1tZWRpYXRlbHkuXG4gICAgaWYgKHRoZW1lUHJvcGVydGllcy5wYXJlbnQpIHtcbiAgICAgIGNvbnN0IGZvdW5kID0gZmluZFRoZW1lRm9sZGVyQW5kSGFuZGxlVGhlbWUodGhlbWVQcm9wZXJ0aWVzLnBhcmVudCwgb3B0aW9ucywgbG9nZ2VyKTtcbiAgICAgIGlmICghZm91bmQpIHtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKFxuICAgICAgICAgIFwiQ291bGQgbm90IGxvY2F0ZSBmaWxlcyBmb3IgZGVmaW5lZCBwYXJlbnQgdGhlbWUgJ1wiICtcbiAgICAgICAgICAgIHRoZW1lUHJvcGVydGllcy5wYXJlbnQgK1xuICAgICAgICAgICAgXCInLlxcblwiICtcbiAgICAgICAgICAgICdQbGVhc2UgdmVyaWZ5IHRoYXQgZGVwZW5kZW5jeSBpcyBhZGRlZCBvciB0aGVtZSBmb2xkZXIgZXhpc3RzLidcbiAgICAgICAgKTtcbiAgICAgIH1cbiAgICB9XG4gICAgY29weVN0YXRpY0Fzc2V0cyh0aGVtZU5hbWUsIHRoZW1lUHJvcGVydGllcywgb3B0aW9ucy5wcm9qZWN0U3RhdGljQXNzZXRzT3V0cHV0Rm9sZGVyLCBsb2dnZXIpO1xuICAgIGNvcHlUaGVtZVJlc291cmNlcyh0aGVtZUZvbGRlciwgb3B0aW9ucy5wcm9qZWN0U3RhdGljQXNzZXRzT3V0cHV0Rm9sZGVyLCBsb2dnZXIpO1xuXG4gICAgd3JpdGVUaGVtZUZpbGVzKHRoZW1lRm9sZGVyLCB0aGVtZU5hbWUsIHRoZW1lUHJvcGVydGllcywgb3B0aW9ucyk7XG4gICAgcmV0dXJuIHRydWU7XG4gIH1cbiAgcmV0dXJuIGZhbHNlO1xufVxuXG5mdW5jdGlvbiBnZXRUaGVtZVByb3BlcnRpZXModGhlbWVGb2xkZXIpIHtcbiAgY29uc3QgdGhlbWVQcm9wZXJ0eUZpbGUgPSByZXNvbHZlKHRoZW1lRm9sZGVyLCAndGhlbWUuanNvbicpO1xuICBpZiAoIWV4aXN0c1N5bmModGhlbWVQcm9wZXJ0eUZpbGUpKSB7XG4gICAgcmV0dXJuIHt9O1xuICB9XG4gIGNvbnN0IHRoZW1lUHJvcGVydHlGaWxlQXNTdHJpbmcgPSByZWFkRmlsZVN5bmModGhlbWVQcm9wZXJ0eUZpbGUpO1xuICBpZiAodGhlbWVQcm9wZXJ0eUZpbGVBc1N0cmluZy5sZW5ndGggPT09IDApIHtcbiAgICByZXR1cm4ge307XG4gIH1cbiAgcmV0dXJuIEpTT04ucGFyc2UodGhlbWVQcm9wZXJ0eUZpbGVBc1N0cmluZyk7XG59XG5cbi8qKlxuICogRXh0cmFjdHMgY3VycmVudCB0aGVtZSBuYW1lIGZyb20gYXV0by1nZW5lcmF0ZWQgJ3RoZW1lLmpzJyBmaWxlIGxvY2F0ZWQgb24gYVxuICogZ2l2ZW4gZm9sZGVyLlxuICogQHBhcmFtIGZyb250ZW5kR2VuZXJhdGVkRm9sZGVyIGZvbGRlciBpbiBwcm9qZWN0IGNvbnRhaW5pbmcgJ3RoZW1lLmpzJyBmaWxlXG4gKiBAcmV0dXJucyB7c3RyaW5nfSBjdXJyZW50IHRoZW1lIG5hbWVcbiAqL1xuZnVuY3Rpb24gZXh0cmFjdFRoZW1lTmFtZShmcm9udGVuZEdlbmVyYXRlZEZvbGRlcikge1xuICBpZiAoIWZyb250ZW5kR2VuZXJhdGVkRm9sZGVyKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKFxuICAgICAgXCJDb3VsZG4ndCBleHRyYWN0IHRoZW1lIG5hbWUgZnJvbSAndGhlbWUuanMnLFwiICtcbiAgICAgICAgJyBiZWNhdXNlIHRoZSBwYXRoIHRvIGZvbGRlciBjb250YWluaW5nIHRoaXMgZmlsZSBpcyBlbXB0eS4gUGxlYXNlIHNldCcgK1xuICAgICAgICAnIHRoZSBhIGNvcnJlY3QgZm9sZGVyIHBhdGggaW4gQXBwbGljYXRpb25UaGVtZVBsdWdpbiBjb25zdHJ1Y3RvcicgK1xuICAgICAgICAnIHBhcmFtZXRlcnMuJ1xuICAgICk7XG4gIH1cbiAgY29uc3QgZ2VuZXJhdGVkVGhlbWVGaWxlID0gcmVzb2x2ZShmcm9udGVuZEdlbmVyYXRlZEZvbGRlciwgJ3RoZW1lLmpzJyk7XG4gIGlmIChleGlzdHNTeW5jKGdlbmVyYXRlZFRoZW1lRmlsZSkpIHtcbiAgICAvLyByZWFkIHRoZW1lIG5hbWUgZnJvbSB0aGUgJ2dlbmVyYXRlZC90aGVtZS5qcycgYXMgdGhlcmUgd2UgYWx3YXlzXG4gICAgLy8gbWFyayB0aGUgdXNlZCB0aGVtZSBmb3Igd2VicGFjayB0byBoYW5kbGUuXG4gICAgY29uc3QgdGhlbWVOYW1lID0gbmFtZVJlZ2V4LmV4ZWMocmVhZEZpbGVTeW5jKGdlbmVyYXRlZFRoZW1lRmlsZSwgeyBlbmNvZGluZzogJ3V0ZjgnIH0pKVsxXTtcbiAgICBpZiAoIXRoZW1lTmFtZSkge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKFwiQ291bGRuJ3QgcGFyc2UgdGhlbWUgbmFtZSBmcm9tICdcIiArIGdlbmVyYXRlZFRoZW1lRmlsZSArIFwiJy5cIik7XG4gICAgfVxuICAgIHJldHVybiB0aGVtZU5hbWU7XG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuICcnO1xuICB9XG59XG5cbi8qKlxuICogRmluZHMgYWxsIHRoZSBwYXJlbnQgdGhlbWVzIGxvY2F0ZWQgaW4gdGhlIHByb2plY3QgdGhlbWVzIGZvbGRlcnMgYW5kIGluXG4gKiB0aGUgSkFSIGRlcGVuZGVuY2llcyB3aXRoIHJlc3BlY3QgdG8gdGhlIGdpdmVuIGN1c3RvbSB0aGVtZSB3aXRoXG4gKiB7QGNvZGUgdGhlbWVOYW1lfS5cbiAqIEBwYXJhbSB7c3RyaW5nfSB0aGVtZU5hbWUgZ2l2ZW4gY3VzdG9tIHRoZW1lIG5hbWUgdG8gbG9vayBwYXJlbnRzIGZvclxuICogQHBhcmFtIHtvYmplY3R9IG9wdGlvbnMgYXBwbGljYXRpb24gdGhlbWUgcGx1Z2luIG1hbmRhdG9yeSBvcHRpb25zLFxuICogQHNlZSB7QGxpbmsgQXBwbGljYXRpb25UaGVtZVBsdWdpbn1cbiAqIEByZXR1cm5zIHtzdHJpbmdbXX0gYXJyYXkgb2YgcGF0aHMgdG8gZm91bmQgcGFyZW50IHRoZW1lcyB3aXRoIHJlc3BlY3QgdG8gdGhlXG4gKiBnaXZlbiBjdXN0b20gdGhlbWVcbiAqL1xuZnVuY3Rpb24gZmluZFBhcmVudFRoZW1lcyh0aGVtZU5hbWUsIG9wdGlvbnMpIHtcbiAgY29uc3QgZXhpc3RpbmdUaGVtZUZvbGRlcnMgPSBbb3B0aW9ucy50aGVtZVJlc291cmNlRm9sZGVyLCAuLi5vcHRpb25zLnRoZW1lUHJvamVjdEZvbGRlcnNdLmZpbHRlcigoZm9sZGVyKSA9PlxuICAgIGV4aXN0c1N5bmMoZm9sZGVyKVxuICApO1xuICByZXR1cm4gY29sbGVjdFBhcmVudFRoZW1lcyh0aGVtZU5hbWUsIGV4aXN0aW5nVGhlbWVGb2xkZXJzLCBmYWxzZSk7XG59XG5cbmZ1bmN0aW9uIGNvbGxlY3RQYXJlbnRUaGVtZXModGhlbWVOYW1lLCB0aGVtZUZvbGRlcnMsIGlzUGFyZW50KSB7XG4gIGxldCBmb3VuZFBhcmVudFRoZW1lcyA9IFtdO1xuICB0aGVtZUZvbGRlcnMuZm9yRWFjaCgoZm9sZGVyKSA9PiB7XG4gICAgY29uc3QgdGhlbWVGb2xkZXIgPSByZXNvbHZlKGZvbGRlciwgdGhlbWVOYW1lKTtcbiAgICBpZiAoZXhpc3RzU3luYyh0aGVtZUZvbGRlcikpIHtcbiAgICAgIGNvbnN0IHRoZW1lUHJvcGVydGllcyA9IGdldFRoZW1lUHJvcGVydGllcyh0aGVtZUZvbGRlcik7XG5cbiAgICAgIGlmICh0aGVtZVByb3BlcnRpZXMucGFyZW50KSB7XG4gICAgICAgIGZvdW5kUGFyZW50VGhlbWVzLnB1c2goLi4uY29sbGVjdFBhcmVudFRoZW1lcyh0aGVtZVByb3BlcnRpZXMucGFyZW50LCB0aGVtZUZvbGRlcnMsIHRydWUpKTtcbiAgICAgICAgaWYgKCFmb3VuZFBhcmVudFRoZW1lcy5sZW5ndGgpIHtcbiAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoXG4gICAgICAgICAgICBcIkNvdWxkIG5vdCBsb2NhdGUgZmlsZXMgZm9yIGRlZmluZWQgcGFyZW50IHRoZW1lICdcIiArXG4gICAgICAgICAgICAgIHRoZW1lUHJvcGVydGllcy5wYXJlbnQgK1xuICAgICAgICAgICAgICBcIicuXFxuXCIgK1xuICAgICAgICAgICAgICAnUGxlYXNlIHZlcmlmeSB0aGF0IGRlcGVuZGVuY3kgaXMgYWRkZWQgb3IgdGhlbWUgZm9sZGVyIGV4aXN0cy4nXG4gICAgICAgICAgKTtcbiAgICAgICAgfVxuICAgICAgfVxuICAgICAgLy8gQWRkIGEgdGhlbWUgcGF0aCB0byByZXN1bHQgY29sbGVjdGlvbiBvbmx5IGlmIGEgZ2l2ZW4gdGhlbWVOYW1lXG4gICAgICAvLyBpcyBzdXBwb3NlZCB0byBiZSBhIHBhcmVudCB0aGVtZVxuICAgICAgaWYgKGlzUGFyZW50KSB7XG4gICAgICAgIGZvdW5kUGFyZW50VGhlbWVzLnB1c2godGhlbWVGb2xkZXIpO1xuICAgICAgfVxuICAgIH1cbiAgfSk7XG4gIHJldHVybiBmb3VuZFBhcmVudFRoZW1lcztcbn1cblxuZXhwb3J0IHsgcHJvY2Vzc1RoZW1lUmVzb3VyY2VzLCBleHRyYWN0VGhlbWVOYW1lLCBmaW5kUGFyZW50VGhlbWVzIH07XG4iLCAiY29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2Rpcm5hbWUgPSBcIkY6XFxcXFByb3llY3RvLVNlZ3VyaWRhZFxcXFxnZXN0aW9uLXNlZ3VyaWRhZC1hZXJvcG9ydHVhcmlhXFxcXHRhcmdldFxcXFxwbHVnaW5zXFxcXGFwcGxpY2F0aW9uLXRoZW1lLXBsdWdpblwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiRjpcXFxcUHJveWVjdG8tU2VndXJpZGFkXFxcXGdlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWFcXFxcdGFyZ2V0XFxcXHBsdWdpbnNcXFxcYXBwbGljYXRpb24tdGhlbWUtcGx1Z2luXFxcXHRoZW1lLWdlbmVyYXRvci5qc1wiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9pbXBvcnRfbWV0YV91cmwgPSBcImZpbGU6Ly8vRjovUHJveWVjdG8tU2VndXJpZGFkL2dlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWEvdGFyZ2V0L3BsdWdpbnMvYXBwbGljYXRpb24tdGhlbWUtcGx1Z2luL3RoZW1lLWdlbmVyYXRvci5qc1wiOy8qXG4gKiBDb3B5cmlnaHQgMjAwMC0yMDI0IFZhYWRpbiBMdGQuXG4gKlxuICogTGljZW5zZWQgdW5kZXIgdGhlIEFwYWNoZSBMaWNlbnNlLCBWZXJzaW9uIDIuMCAodGhlIFwiTGljZW5zZVwiKTsgeW91IG1heSBub3RcbiAqIHVzZSB0aGlzIGZpbGUgZXhjZXB0IGluIGNvbXBsaWFuY2Ugd2l0aCB0aGUgTGljZW5zZS4gWW91IG1heSBvYnRhaW4gYSBjb3B5IG9mXG4gKiB0aGUgTGljZW5zZSBhdFxuICpcbiAqIGh0dHA6Ly93d3cuYXBhY2hlLm9yZy9saWNlbnNlcy9MSUNFTlNFLTIuMFxuICpcbiAqIFVubGVzcyByZXF1aXJlZCBieSBhcHBsaWNhYmxlIGxhdyBvciBhZ3JlZWQgdG8gaW4gd3JpdGluZywgc29mdHdhcmVcbiAqIGRpc3RyaWJ1dGVkIHVuZGVyIHRoZSBMaWNlbnNlIGlzIGRpc3RyaWJ1dGVkIG9uIGFuIFwiQVMgSVNcIiBCQVNJUywgV0lUSE9VVFxuICogV0FSUkFOVElFUyBPUiBDT05ESVRJT05TIE9GIEFOWSBLSU5ELCBlaXRoZXIgZXhwcmVzcyBvciBpbXBsaWVkLiBTZWUgdGhlXG4gKiBMaWNlbnNlIGZvciB0aGUgc3BlY2lmaWMgbGFuZ3VhZ2UgZ292ZXJuaW5nIHBlcm1pc3Npb25zIGFuZCBsaW1pdGF0aW9ucyB1bmRlclxuICogdGhlIExpY2Vuc2UuXG4gKi9cblxuLyoqXG4gKiBUaGlzIGZpbGUgaGFuZGxlcyB0aGUgZ2VuZXJhdGlvbiBvZiB0aGUgJ1t0aGVtZS1uYW1lXS5qcycgdG9cbiAqIHRoZSB0aGVtZXMvW3RoZW1lLW5hbWVdIGZvbGRlciBhY2NvcmRpbmcgdG8gcHJvcGVydGllcyBmcm9tICd0aGVtZS5qc29uJy5cbiAqL1xuaW1wb3J0IHsgZ2xvYlN5bmMgfSBmcm9tICdnbG9iJztcbmltcG9ydCB7IHJlc29sdmUsIGJhc2VuYW1lIH0gZnJvbSAncGF0aCc7XG5pbXBvcnQgeyBleGlzdHNTeW5jLCByZWFkRmlsZVN5bmMsIHdyaXRlRmlsZVN5bmMgfSBmcm9tICdmcyc7XG5pbXBvcnQgeyBjaGVja01vZHVsZXMgfSBmcm9tICcuL3RoZW1lLWNvcHkuanMnO1xuXG4vLyBTcGVjaWFsIGZvbGRlciBpbnNpZGUgYSB0aGVtZSBmb3IgY29tcG9uZW50IHRoZW1lcyB0aGF0IGdvIGluc2lkZSB0aGUgY29tcG9uZW50IHNoYWRvdyByb290XG5jb25zdCB0aGVtZUNvbXBvbmVudHNGb2xkZXIgPSAnY29tcG9uZW50cyc7XG4vLyBUaGUgY29udGVudHMgb2YgYSBnbG9iYWwgQ1NTIGZpbGUgd2l0aCB0aGlzIG5hbWUgaW4gYSB0aGVtZSBpcyBhbHdheXMgYWRkZWQgdG9cbi8vIHRoZSBkb2N1bWVudC4gRS5nLiBAZm9udC1mYWNlIG11c3QgYmUgaW4gdGhpc1xuY29uc3QgZG9jdW1lbnRDc3NGaWxlbmFtZSA9ICdkb2N1bWVudC5jc3MnO1xuLy8gc3R5bGVzLmNzcyBpcyB0aGUgb25seSBlbnRyeXBvaW50IGNzcyBmaWxlIHdpdGggZG9jdW1lbnQuY3NzLiBFdmVyeXRoaW5nIGVsc2Ugc2hvdWxkIGJlIGltcG9ydGVkIHVzaW5nIGNzcyBAaW1wb3J0XG5jb25zdCBzdHlsZXNDc3NGaWxlbmFtZSA9ICdzdHlsZXMuY3NzJztcblxuY29uc3QgQ1NTSU1QT1JUX0NPTU1FTlQgPSAnQ1NTSW1wb3J0IGVuZCc7XG5jb25zdCBoZWFkZXJJbXBvcnQgPSBgaW1wb3J0ICdjb25zdHJ1Y3Qtc3R5bGUtc2hlZXRzLXBvbHlmaWxsJztcbmA7XG5cbi8qKlxuICogR2VuZXJhdGUgdGhlIFt0aGVtZU5hbWVdLmpzIGZpbGUgZm9yIHRoZW1lRm9sZGVyIHdoaWNoIGNvbGxlY3RzIGFsbCByZXF1aXJlZCBpbmZvcm1hdGlvbiBmcm9tIHRoZSBmb2xkZXIuXG4gKlxuICogQHBhcmFtIHtzdHJpbmd9IHRoZW1lRm9sZGVyIGZvbGRlciBvZiB0aGUgdGhlbWVcbiAqIEBwYXJhbSB7c3RyaW5nfSB0aGVtZU5hbWUgbmFtZSBvZiB0aGUgaGFuZGxlZCB0aGVtZVxuICogQHBhcmFtIHtKU09OfSB0aGVtZVByb3BlcnRpZXMgY29udGVudCBvZiB0aGVtZS5qc29uXG4gKiBAcGFyYW0ge09iamVjdH0gb3B0aW9ucyBidWlsZCBvcHRpb25zIChlLmcuIHByb2Qgb3IgZGV2IG1vZGUpXG4gKiBAcmV0dXJucyB7c3RyaW5nfSB0aGVtZSBmaWxlIGNvbnRlbnRcbiAqL1xuZnVuY3Rpb24gd3JpdGVUaGVtZUZpbGVzKHRoZW1lRm9sZGVyLCB0aGVtZU5hbWUsIHRoZW1lUHJvcGVydGllcywgb3B0aW9ucykge1xuICBjb25zdCBwcm9kdWN0aW9uTW9kZSA9ICFvcHRpb25zLmRldk1vZGU7XG4gIGNvbnN0IHVzZURldlNlcnZlck9ySW5Qcm9kdWN0aW9uTW9kZSA9ICFvcHRpb25zLnVzZURldkJ1bmRsZTtcbiAgY29uc3Qgb3V0cHV0Rm9sZGVyID0gb3B0aW9ucy5mcm9udGVuZEdlbmVyYXRlZEZvbGRlcjtcbiAgY29uc3Qgc3R5bGVzID0gcmVzb2x2ZSh0aGVtZUZvbGRlciwgc3R5bGVzQ3NzRmlsZW5hbWUpO1xuICBjb25zdCBkb2N1bWVudENzc0ZpbGUgPSByZXNvbHZlKHRoZW1lRm9sZGVyLCBkb2N1bWVudENzc0ZpbGVuYW1lKTtcbiAgY29uc3QgYXV0b0luamVjdENvbXBvbmVudHMgPSB0aGVtZVByb3BlcnRpZXMuYXV0b0luamVjdENvbXBvbmVudHMgPz8gdHJ1ZTtcbiAgY29uc3QgZ2xvYmFsRmlsZW5hbWUgPSAndGhlbWUtJyArIHRoZW1lTmFtZSArICcuZ2xvYmFsLmdlbmVyYXRlZC5qcyc7XG4gIGNvbnN0IGNvbXBvbmVudHNGaWxlbmFtZSA9ICd0aGVtZS0nICsgdGhlbWVOYW1lICsgJy5jb21wb25lbnRzLmdlbmVyYXRlZC5qcyc7XG4gIGNvbnN0IHRoZW1lRmlsZW5hbWUgPSAndGhlbWUtJyArIHRoZW1lTmFtZSArICcuZ2VuZXJhdGVkLmpzJztcblxuICBsZXQgdGhlbWVGaWxlQ29udGVudCA9IGhlYWRlckltcG9ydDtcbiAgbGV0IGdsb2JhbEltcG9ydENvbnRlbnQgPSAnLy8gV2hlbiB0aGlzIGZpbGUgaXMgaW1wb3J0ZWQsIGdsb2JhbCBzdHlsZXMgYXJlIGF1dG9tYXRpY2FsbHkgYXBwbGllZFxcbic7XG4gIGxldCBjb21wb25lbnRzRmlsZUNvbnRlbnQgPSAnJztcbiAgdmFyIGNvbXBvbmVudHNGaWxlcztcblxuICBpZiAoYXV0b0luamVjdENvbXBvbmVudHMpIHtcbiAgICBjb21wb25lbnRzRmlsZXMgPSBnbG9iU3luYygnKi5jc3MnLCB7XG4gICAgICBjd2Q6IHJlc29sdmUodGhlbWVGb2xkZXIsIHRoZW1lQ29tcG9uZW50c0ZvbGRlciksXG4gICAgICBub2RpcjogdHJ1ZVxuICAgIH0pO1xuXG4gICAgaWYgKGNvbXBvbmVudHNGaWxlcy5sZW5ndGggPiAwKSB7XG4gICAgICBjb21wb25lbnRzRmlsZUNvbnRlbnQgKz1cbiAgICAgICAgXCJpbXBvcnQgeyB1bnNhZmVDU1MsIHJlZ2lzdGVyU3R5bGVzIH0gZnJvbSAnQHZhYWRpbi92YWFkaW4tdGhlbWFibGUtbWl4aW4vcmVnaXN0ZXItc3R5bGVzJztcXG5cIjtcbiAgICB9XG4gIH1cblxuICBpZiAodGhlbWVQcm9wZXJ0aWVzLnBhcmVudCkge1xuICAgIHRoZW1lRmlsZUNvbnRlbnQgKz0gYGltcG9ydCB7IGFwcGx5VGhlbWUgYXMgYXBwbHlCYXNlVGhlbWUgfSBmcm9tICcuL3RoZW1lLSR7dGhlbWVQcm9wZXJ0aWVzLnBhcmVudH0uZ2VuZXJhdGVkLmpzJztcXG5gO1xuICB9XG5cbiAgdGhlbWVGaWxlQ29udGVudCArPSBgaW1wb3J0IHsgaW5qZWN0R2xvYmFsQ3NzIH0gZnJvbSAnRnJvbnRlbmQvZ2VuZXJhdGVkL2phci1yZXNvdXJjZXMvdGhlbWUtdXRpbC5qcyc7XFxuYDtcbiAgdGhlbWVGaWxlQ29udGVudCArPSBgaW1wb3J0ICcuLyR7Y29tcG9uZW50c0ZpbGVuYW1lfSc7XFxuYDtcblxuICB0aGVtZUZpbGVDb250ZW50ICs9IGBsZXQgbmVlZHNSZWxvYWRPbkNoYW5nZXMgPSBmYWxzZTtcXG5gO1xuICBjb25zdCBpbXBvcnRzID0gW107XG4gIGNvbnN0IGNvbXBvbmVudENzc0ltcG9ydHMgPSBbXTtcbiAgY29uc3QgZ2xvYmFsRmlsZUNvbnRlbnQgPSBbXTtcbiAgY29uc3QgZ2xvYmFsQ3NzQ29kZSA9IFtdO1xuICBjb25zdCBzaGFkb3dPbmx5Q3NzID0gW107XG4gIGNvbnN0IGNvbXBvbmVudENzc0NvZGUgPSBbXTtcbiAgY29uc3QgcGFyZW50VGhlbWUgPSB0aGVtZVByb3BlcnRpZXMucGFyZW50ID8gJ2FwcGx5QmFzZVRoZW1lKHRhcmdldCk7XFxuJyA6ICcnO1xuICBjb25zdCBwYXJlbnRUaGVtZUdsb2JhbEltcG9ydCA9IHRoZW1lUHJvcGVydGllcy5wYXJlbnRcbiAgICA/IGBpbXBvcnQgJy4vdGhlbWUtJHt0aGVtZVByb3BlcnRpZXMucGFyZW50fS5nbG9iYWwuZ2VuZXJhdGVkLmpzJztcXG5gXG4gICAgOiAnJztcblxuICBjb25zdCB0aGVtZUlkZW50aWZpZXIgPSAnX3ZhYWRpbnRoZW1lXycgKyB0aGVtZU5hbWUgKyAnXyc7XG4gIGNvbnN0IGx1bW9Dc3NGbGFnID0gJ192YWFkaW50aGVtZWx1bW9pbXBvcnRzXyc7XG4gIGNvbnN0IGdsb2JhbENzc0ZsYWcgPSB0aGVtZUlkZW50aWZpZXIgKyAnZ2xvYmFsQ3NzJztcbiAgY29uc3QgY29tcG9uZW50Q3NzRmxhZyA9IHRoZW1lSWRlbnRpZmllciArICdjb21wb25lbnRDc3MnO1xuXG4gIGlmICghZXhpc3RzU3luYyhzdHlsZXMpKSB7XG4gICAgaWYgKHByb2R1Y3Rpb25Nb2RlKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoYHN0eWxlcy5jc3MgZmlsZSBpcyBtaXNzaW5nIGFuZCBpcyBuZWVkZWQgZm9yICcke3RoZW1lTmFtZX0nIGluIGZvbGRlciAnJHt0aGVtZUZvbGRlcn0nYCk7XG4gICAgfVxuICAgIHdyaXRlRmlsZVN5bmMoXG4gICAgICBzdHlsZXMsXG4gICAgICAnLyogSW1wb3J0IHlvdXIgYXBwbGljYXRpb24gZ2xvYmFsIGNzcyBmaWxlcyBoZXJlIG9yIGFkZCB0aGUgc3R5bGVzIGRpcmVjdGx5IHRvIHRoaXMgZmlsZSAqLycsXG4gICAgICAndXRmOCdcbiAgICApO1xuICB9XG5cbiAgLy8gc3R5bGVzLmNzcyB3aWxsIGFsd2F5cyBiZSBhdmFpbGFibGUgYXMgd2Ugd3JpdGUgb25lIGlmIGl0IGRvZXNuJ3QgZXhpc3QuXG4gIGxldCBmaWxlbmFtZSA9IGJhc2VuYW1lKHN0eWxlcyk7XG4gIGxldCB2YXJpYWJsZSA9IGNhbWVsQ2FzZShmaWxlbmFtZSk7XG5cbiAgLyogTFVNTyAqL1xuICBjb25zdCBsdW1vSW1wb3J0cyA9IHRoZW1lUHJvcGVydGllcy5sdW1vSW1wb3J0cyB8fCBbJ2NvbG9yJywgJ3R5cG9ncmFwaHknXTtcbiAgaWYgKGx1bW9JbXBvcnRzKSB7XG4gICAgbHVtb0ltcG9ydHMuZm9yRWFjaCgobHVtb0ltcG9ydCkgPT4ge1xuICAgICAgaW1wb3J0cy5wdXNoKGBpbXBvcnQgeyAke2x1bW9JbXBvcnR9IH0gZnJvbSAnQHZhYWRpbi92YWFkaW4tbHVtby1zdHlsZXMvJHtsdW1vSW1wb3J0fS5qcyc7XFxuYCk7XG4gICAgICBpZiAobHVtb0ltcG9ydCA9PT0gJ3V0aWxpdHknIHx8IGx1bW9JbXBvcnQgPT09ICdiYWRnZScgfHwgbHVtb0ltcG9ydCA9PT0gJ3R5cG9ncmFwaHknIHx8IGx1bW9JbXBvcnQgPT09ICdjb2xvcicpIHtcbiAgICAgICAgLy8gSW5qZWN0IGludG8gbWFpbiBkb2N1bWVudCB0aGUgc2FtZSB3YXkgYXMgb3RoZXIgTHVtbyBzdHlsZXMgYXJlIGluamVjdGVkXG4gICAgICAgIC8vIEx1bW8gaW1wb3J0cyBnbyB0byB0aGUgdGhlbWUgZ2xvYmFsIGltcG9ydHMgZmlsZSB0byBwcmV2ZW50IHN0eWxlIGxlYWtzXG4gICAgICAgIC8vIHdoZW4gdGhlIHRoZW1lIGlzIGFwcGxpZWQgdG8gYW4gZW1iZWRkZWQgY29tcG9uZW50XG4gICAgICAgIGdsb2JhbEZpbGVDb250ZW50LnB1c2goYGltcG9ydCAnQHZhYWRpbi92YWFkaW4tbHVtby1zdHlsZXMvJHtsdW1vSW1wb3J0fS1nbG9iYWwuanMnO1xcbmApO1xuICAgICAgfVxuICAgIH0pO1xuXG4gICAgbHVtb0ltcG9ydHMuZm9yRWFjaCgobHVtb0ltcG9ydCkgPT4ge1xuICAgICAgLy8gTHVtbyBpcyBpbmplY3RlZCB0byB0aGUgZG9jdW1lbnQgYnkgTHVtbyBpdHNlbGZcbiAgICAgIHNoYWRvd09ubHlDc3MucHVzaChgcmVtb3ZlcnMucHVzaChpbmplY3RHbG9iYWxDc3MoJHtsdW1vSW1wb3J0fS5jc3NUZXh0LCAnJywgdGFyZ2V0LCB0cnVlKSk7XFxuYCk7XG4gICAgfSk7XG4gIH1cblxuICAvKiBUaGVtZSAqL1xuICBpZiAodXNlRGV2U2VydmVyT3JJblByb2R1Y3Rpb25Nb2RlKSB7XG4gICAgZ2xvYmFsRmlsZUNvbnRlbnQucHVzaChwYXJlbnRUaGVtZUdsb2JhbEltcG9ydCk7XG4gICAgZ2xvYmFsRmlsZUNvbnRlbnQucHVzaChgaW1wb3J0ICd0aGVtZXMvJHt0aGVtZU5hbWV9LyR7ZmlsZW5hbWV9JztcXG5gKTtcblxuICAgIGltcG9ydHMucHVzaChgaW1wb3J0ICR7dmFyaWFibGV9IGZyb20gJ3RoZW1lcy8ke3RoZW1lTmFtZX0vJHtmaWxlbmFtZX0/aW5saW5lJztcXG5gKTtcbiAgICBzaGFkb3dPbmx5Q3NzLnB1c2goYHJlbW92ZXJzLnB1c2goaW5qZWN0R2xvYmFsQ3NzKCR7dmFyaWFibGV9LnRvU3RyaW5nKCksICcnLCB0YXJnZXQpKTtcXG4gICAgYCk7XG4gIH1cbiAgaWYgKGV4aXN0c1N5bmMoZG9jdW1lbnRDc3NGaWxlKSkge1xuICAgIGZpbGVuYW1lID0gYmFzZW5hbWUoZG9jdW1lbnRDc3NGaWxlKTtcbiAgICB2YXJpYWJsZSA9IGNhbWVsQ2FzZShmaWxlbmFtZSk7XG5cbiAgICBpZiAodXNlRGV2U2VydmVyT3JJblByb2R1Y3Rpb25Nb2RlKSB7XG4gICAgICBnbG9iYWxGaWxlQ29udGVudC5wdXNoKGBpbXBvcnQgJ3RoZW1lcy8ke3RoZW1lTmFtZX0vJHtmaWxlbmFtZX0nO1xcbmApO1xuXG4gICAgICBpbXBvcnRzLnB1c2goYGltcG9ydCAke3ZhcmlhYmxlfSBmcm9tICd0aGVtZXMvJHt0aGVtZU5hbWV9LyR7ZmlsZW5hbWV9P2lubGluZSc7XFxuYCk7XG4gICAgICBzaGFkb3dPbmx5Q3NzLnB1c2goYHJlbW92ZXJzLnB1c2goaW5qZWN0R2xvYmFsQ3NzKCR7dmFyaWFibGV9LnRvU3RyaW5nKCksJycsIGRvY3VtZW50KSk7XFxuICAgIGApO1xuICAgIH1cbiAgfVxuXG4gIGxldCBpID0gMDtcbiAgaWYgKHRoZW1lUHJvcGVydGllcy5kb2N1bWVudENzcykge1xuICAgIGNvbnN0IG1pc3NpbmdNb2R1bGVzID0gY2hlY2tNb2R1bGVzKHRoZW1lUHJvcGVydGllcy5kb2N1bWVudENzcyk7XG4gICAgaWYgKG1pc3NpbmdNb2R1bGVzLmxlbmd0aCA+IDApIHtcbiAgICAgIHRocm93IEVycm9yKFxuICAgICAgICBcIk1pc3NpbmcgbnBtIG1vZHVsZXMgb3IgZmlsZXMgJ1wiICtcbiAgICAgICAgICBtaXNzaW5nTW9kdWxlcy5qb2luKFwiJywgJ1wiKSArXG4gICAgICAgICAgXCInIGZvciBkb2N1bWVudENzcyBtYXJrZWQgaW4gJ3RoZW1lLmpzb24nLlxcblwiICtcbiAgICAgICAgICBcIkluc3RhbGwgb3IgdXBkYXRlIHBhY2thZ2UocykgYnkgYWRkaW5nIGEgQE5wbVBhY2thZ2UgYW5ub3RhdGlvbiBvciBpbnN0YWxsIGl0IHVzaW5nICducG0vcG5wbS9idW4gaSdcIlxuICAgICAgKTtcbiAgICB9XG4gICAgdGhlbWVQcm9wZXJ0aWVzLmRvY3VtZW50Q3NzLmZvckVhY2goKGNzc0ltcG9ydCkgPT4ge1xuICAgICAgY29uc3QgdmFyaWFibGUgPSAnbW9kdWxlJyArIGkrKztcbiAgICAgIGltcG9ydHMucHVzaChgaW1wb3J0ICR7dmFyaWFibGV9IGZyb20gJyR7Y3NzSW1wb3J0fT9pbmxpbmUnO1xcbmApO1xuICAgICAgLy8gRHVlIHRvIGNocm9tZSBidWcgaHR0cHM6Ly9idWdzLmNocm9taXVtLm9yZy9wL2Nocm9taXVtL2lzc3Vlcy9kZXRhaWw/aWQ9MzM2ODc2IGZvbnQtZmFjZSB3aWxsIG5vdCB3b3JrXG4gICAgICAvLyBpbnNpZGUgc2hhZG93Um9vdCBzbyB3ZSBuZWVkIHRvIGluamVjdCBpdCB0aGVyZSBhbHNvLlxuICAgICAgZ2xvYmFsQ3NzQ29kZS5wdXNoKGBpZih0YXJnZXQgIT09IGRvY3VtZW50KSB7XG4gICAgICAgIHJlbW92ZXJzLnB1c2goaW5qZWN0R2xvYmFsQ3NzKCR7dmFyaWFibGV9LnRvU3RyaW5nKCksICcnLCB0YXJnZXQpKTtcbiAgICB9XFxuICAgIGApO1xuICAgICAgZ2xvYmFsQ3NzQ29kZS5wdXNoKFxuICAgICAgICBgcmVtb3ZlcnMucHVzaChpbmplY3RHbG9iYWxDc3MoJHt2YXJpYWJsZX0udG9TdHJpbmcoKSwgJyR7Q1NTSU1QT1JUX0NPTU1FTlR9JywgZG9jdW1lbnQpKTtcXG4gICAgYFxuICAgICAgKTtcbiAgICB9KTtcbiAgfVxuICBpZiAodGhlbWVQcm9wZXJ0aWVzLmltcG9ydENzcykge1xuICAgIGNvbnN0IG1pc3NpbmdNb2R1bGVzID0gY2hlY2tNb2R1bGVzKHRoZW1lUHJvcGVydGllcy5pbXBvcnRDc3MpO1xuICAgIGlmIChtaXNzaW5nTW9kdWxlcy5sZW5ndGggPiAwKSB7XG4gICAgICB0aHJvdyBFcnJvcihcbiAgICAgICAgXCJNaXNzaW5nIG5wbSBtb2R1bGVzIG9yIGZpbGVzICdcIiArXG4gICAgICAgICAgbWlzc2luZ01vZHVsZXMuam9pbihcIicsICdcIikgK1xuICAgICAgICAgIFwiJyBmb3IgaW1wb3J0Q3NzIG1hcmtlZCBpbiAndGhlbWUuanNvbicuXFxuXCIgK1xuICAgICAgICAgIFwiSW5zdGFsbCBvciB1cGRhdGUgcGFja2FnZShzKSBieSBhZGRpbmcgYSBATnBtUGFja2FnZSBhbm5vdGF0aW9uIG9yIGluc3RhbGwgaXQgdXNpbmcgJ25wbS9wbnBtL2J1biBpJ1wiXG4gICAgICApO1xuICAgIH1cbiAgICB0aGVtZVByb3BlcnRpZXMuaW1wb3J0Q3NzLmZvckVhY2goKGNzc1BhdGgpID0+IHtcbiAgICAgIGNvbnN0IHZhcmlhYmxlID0gJ21vZHVsZScgKyBpKys7XG4gICAgICBnbG9iYWxGaWxlQ29udGVudC5wdXNoKGBpbXBvcnQgJyR7Y3NzUGF0aH0nO1xcbmApO1xuICAgICAgaW1wb3J0cy5wdXNoKGBpbXBvcnQgJHt2YXJpYWJsZX0gZnJvbSAnJHtjc3NQYXRofT9pbmxpbmUnO1xcbmApO1xuICAgICAgc2hhZG93T25seUNzcy5wdXNoKGByZW1vdmVycy5wdXNoKGluamVjdEdsb2JhbENzcygke3ZhcmlhYmxlfS50b1N0cmluZygpLCAnJHtDU1NJTVBPUlRfQ09NTUVOVH0nLCB0YXJnZXQpKTtcXG5gKTtcbiAgICB9KTtcbiAgfVxuXG4gIGlmIChhdXRvSW5qZWN0Q29tcG9uZW50cykge1xuICAgIGNvbXBvbmVudHNGaWxlcy5mb3JFYWNoKChjb21wb25lbnRDc3MpID0+IHtcbiAgICAgIGNvbnN0IGZpbGVuYW1lID0gYmFzZW5hbWUoY29tcG9uZW50Q3NzKTtcbiAgICAgIGNvbnN0IHRhZyA9IGZpbGVuYW1lLnJlcGxhY2UoJy5jc3MnLCAnJyk7XG4gICAgICBjb25zdCB2YXJpYWJsZSA9IGNhbWVsQ2FzZShmaWxlbmFtZSk7XG4gICAgICBjb21wb25lbnRDc3NJbXBvcnRzLnB1c2goXG4gICAgICAgIGBpbXBvcnQgJHt2YXJpYWJsZX0gZnJvbSAndGhlbWVzLyR7dGhlbWVOYW1lfS8ke3RoZW1lQ29tcG9uZW50c0ZvbGRlcn0vJHtmaWxlbmFtZX0/aW5saW5lJztcXG5gXG4gICAgICApO1xuICAgICAgLy8gRG9uJ3QgZm9ybWF0IGFzIHRoZSBnZW5lcmF0ZWQgZmlsZSBmb3JtYXR0aW5nIHdpbGwgZ2V0IHdvbmt5IVxuICAgICAgY29uc3QgY29tcG9uZW50U3RyaW5nID0gYHJlZ2lzdGVyU3R5bGVzKFxuICAgICAgICAnJHt0YWd9JyxcbiAgICAgICAgdW5zYWZlQ1NTKCR7dmFyaWFibGV9LnRvU3RyaW5nKCkpXG4gICAgICApO1xuICAgICAgYDtcbiAgICAgIGNvbXBvbmVudENzc0NvZGUucHVzaChjb21wb25lbnRTdHJpbmcpO1xuICAgIH0pO1xuICB9XG5cbiAgdGhlbWVGaWxlQ29udGVudCArPSBpbXBvcnRzLmpvaW4oJycpO1xuXG4gIC8vIERvbid0IGZvcm1hdCBhcyB0aGUgZ2VuZXJhdGVkIGZpbGUgZm9ybWF0dGluZyB3aWxsIGdldCB3b25reSFcbiAgLy8gSWYgdGFyZ2V0cyBjaGVjayB0aGF0IHdlIG9ubHkgcmVnaXN0ZXIgdGhlIHN0eWxlIHBhcnRzIG9uY2UsIGNoZWNrcyBleGlzdCBmb3IgZ2xvYmFsIGNzcyBhbmQgY29tcG9uZW50IGNzc1xuICBjb25zdCB0aGVtZUZpbGVBcHBseSA9IGBcbiAgbGV0IHRoZW1lUmVtb3ZlcnMgPSBuZXcgV2Vha01hcCgpO1xuICBsZXQgdGFyZ2V0cyA9IFtdO1xuXG4gIGV4cG9ydCBjb25zdCBhcHBseVRoZW1lID0gKHRhcmdldCkgPT4ge1xuICAgIGNvbnN0IHJlbW92ZXJzID0gW107XG4gICAgaWYgKHRhcmdldCAhPT0gZG9jdW1lbnQpIHtcbiAgICAgICR7c2hhZG93T25seUNzcy5qb2luKCcnKX1cbiAgICB9XG4gICAgJHtwYXJlbnRUaGVtZX1cbiAgICAke2dsb2JhbENzc0NvZGUuam9pbignJyl9XG5cbiAgICBpZiAoaW1wb3J0Lm1ldGEuaG90KSB7XG4gICAgICB0YXJnZXRzLnB1c2gobmV3IFdlYWtSZWYodGFyZ2V0KSk7XG4gICAgICB0aGVtZVJlbW92ZXJzLnNldCh0YXJnZXQsIHJlbW92ZXJzKTtcbiAgICB9XG5cbiAgfVxuICBcbmA7XG4gIGNvbXBvbmVudHNGaWxlQ29udGVudCArPSBgXG4ke2NvbXBvbmVudENzc0ltcG9ydHMuam9pbignJyl9XG5cbmlmICghZG9jdW1lbnRbJyR7Y29tcG9uZW50Q3NzRmxhZ30nXSkge1xuICAke2NvbXBvbmVudENzc0NvZGUuam9pbignJyl9XG4gIGRvY3VtZW50Wycke2NvbXBvbmVudENzc0ZsYWd9J10gPSB0cnVlO1xufVxuXG5pZiAoaW1wb3J0Lm1ldGEuaG90KSB7XG4gIGltcG9ydC5tZXRhLmhvdC5hY2NlcHQoKG1vZHVsZSkgPT4ge1xuICAgIHdpbmRvdy5sb2NhdGlvbi5yZWxvYWQoKTtcbiAgfSk7XG59XG5cbmA7XG5cbiAgdGhlbWVGaWxlQ29udGVudCArPSB0aGVtZUZpbGVBcHBseTtcbiAgdGhlbWVGaWxlQ29udGVudCArPSBgXG5pZiAoaW1wb3J0Lm1ldGEuaG90KSB7XG4gIGltcG9ydC5tZXRhLmhvdC5hY2NlcHQoKG1vZHVsZSkgPT4ge1xuXG4gICAgaWYgKG5lZWRzUmVsb2FkT25DaGFuZ2VzKSB7XG4gICAgICB3aW5kb3cubG9jYXRpb24ucmVsb2FkKCk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHRhcmdldHMuZm9yRWFjaCh0YXJnZXRSZWYgPT4ge1xuICAgICAgICBjb25zdCB0YXJnZXQgPSB0YXJnZXRSZWYuZGVyZWYoKTtcbiAgICAgICAgaWYgKHRhcmdldCkge1xuICAgICAgICAgIHRoZW1lUmVtb3ZlcnMuZ2V0KHRhcmdldCkuZm9yRWFjaChyZW1vdmVyID0+IHJlbW92ZXIoKSlcbiAgICAgICAgICBtb2R1bGUuYXBwbHlUaGVtZSh0YXJnZXQpO1xuICAgICAgICB9XG4gICAgICB9KVxuICAgIH1cbiAgfSk7XG5cbiAgaW1wb3J0Lm1ldGEuaG90Lm9uKCd2aXRlOmFmdGVyVXBkYXRlJywgKHVwZGF0ZSkgPT4ge1xuICAgIGRvY3VtZW50LmRpc3BhdGNoRXZlbnQobmV3IEN1c3RvbUV2ZW50KCd2YWFkaW4tdGhlbWUtdXBkYXRlZCcsIHsgZGV0YWlsOiB1cGRhdGUgfSkpO1xuICB9KTtcbn1cblxuYDtcblxuICBnbG9iYWxJbXBvcnRDb250ZW50ICs9IGBcbiR7Z2xvYmFsRmlsZUNvbnRlbnQuam9pbignJyl9XG5gO1xuXG4gIHdyaXRlSWZDaGFuZ2VkKHJlc29sdmUob3V0cHV0Rm9sZGVyLCBnbG9iYWxGaWxlbmFtZSksIGdsb2JhbEltcG9ydENvbnRlbnQpO1xuICB3cml0ZUlmQ2hhbmdlZChyZXNvbHZlKG91dHB1dEZvbGRlciwgdGhlbWVGaWxlbmFtZSksIHRoZW1lRmlsZUNvbnRlbnQpO1xuICB3cml0ZUlmQ2hhbmdlZChyZXNvbHZlKG91dHB1dEZvbGRlciwgY29tcG9uZW50c0ZpbGVuYW1lKSwgY29tcG9uZW50c0ZpbGVDb250ZW50KTtcbn1cblxuZnVuY3Rpb24gd3JpdGVJZkNoYW5nZWQoZmlsZSwgZGF0YSkge1xuICBpZiAoIWV4aXN0c1N5bmMoZmlsZSkgfHwgcmVhZEZpbGVTeW5jKGZpbGUsIHsgZW5jb2Rpbmc6ICd1dGYtOCcgfSkgIT09IGRhdGEpIHtcbiAgICB3cml0ZUZpbGVTeW5jKGZpbGUsIGRhdGEpO1xuICB9XG59XG5cbi8qKlxuICogTWFrZSBnaXZlbiBzdHJpbmcgaW50byBjYW1lbENhc2UuXG4gKlxuICogQHBhcmFtIHtzdHJpbmd9IHN0ciBzdHJpbmcgdG8gbWFrZSBpbnRvIGNhbWVDYXNlXG4gKiBAcmV0dXJucyB7c3RyaW5nfSBjYW1lbENhc2VkIHZlcnNpb25cbiAqL1xuZnVuY3Rpb24gY2FtZWxDYXNlKHN0cikge1xuICByZXR1cm4gc3RyXG4gICAgLnJlcGxhY2UoLyg/Ol5cXHd8W0EtWl18XFxiXFx3KS9nLCBmdW5jdGlvbiAod29yZCwgaW5kZXgpIHtcbiAgICAgIHJldHVybiBpbmRleCA9PT0gMCA/IHdvcmQudG9Mb3dlckNhc2UoKSA6IHdvcmQudG9VcHBlckNhc2UoKTtcbiAgICB9KVxuICAgIC5yZXBsYWNlKC9cXHMrL2csICcnKVxuICAgIC5yZXBsYWNlKC9cXC58XFwtL2csICcnKTtcbn1cblxuZXhwb3J0IHsgd3JpdGVUaGVtZUZpbGVzIH07XG4iLCAiY29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2Rpcm5hbWUgPSBcIkY6XFxcXFByb3llY3RvLVNlZ3VyaWRhZFxcXFxnZXN0aW9uLXNlZ3VyaWRhZC1hZXJvcG9ydHVhcmlhXFxcXHRhcmdldFxcXFxwbHVnaW5zXFxcXGFwcGxpY2F0aW9uLXRoZW1lLXBsdWdpblwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiRjpcXFxcUHJveWVjdG8tU2VndXJpZGFkXFxcXGdlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWFcXFxcdGFyZ2V0XFxcXHBsdWdpbnNcXFxcYXBwbGljYXRpb24tdGhlbWUtcGx1Z2luXFxcXHRoZW1lLWNvcHkuanNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0Y6L1Byb3llY3RvLVNlZ3VyaWRhZC9nZXN0aW9uLXNlZ3VyaWRhZC1hZXJvcG9ydHVhcmlhL3RhcmdldC9wbHVnaW5zL2FwcGxpY2F0aW9uLXRoZW1lLXBsdWdpbi90aGVtZS1jb3B5LmpzXCI7LypcbiAqIENvcHlyaWdodCAyMDAwLTIwMjQgVmFhZGluIEx0ZC5cbiAqXG4gKiBMaWNlbnNlZCB1bmRlciB0aGUgQXBhY2hlIExpY2Vuc2UsIFZlcnNpb24gMi4wICh0aGUgXCJMaWNlbnNlXCIpOyB5b3UgbWF5IG5vdFxuICogdXNlIHRoaXMgZmlsZSBleGNlcHQgaW4gY29tcGxpYW5jZSB3aXRoIHRoZSBMaWNlbnNlLiBZb3UgbWF5IG9idGFpbiBhIGNvcHkgb2ZcbiAqIHRoZSBMaWNlbnNlIGF0XG4gKlxuICogaHR0cDovL3d3dy5hcGFjaGUub3JnL2xpY2Vuc2VzL0xJQ0VOU0UtMi4wXG4gKlxuICogVW5sZXNzIHJlcXVpcmVkIGJ5IGFwcGxpY2FibGUgbGF3IG9yIGFncmVlZCB0byBpbiB3cml0aW5nLCBzb2Z0d2FyZVxuICogZGlzdHJpYnV0ZWQgdW5kZXIgdGhlIExpY2Vuc2UgaXMgZGlzdHJpYnV0ZWQgb24gYW4gXCJBUyBJU1wiIEJBU0lTLCBXSVRIT1VUXG4gKiBXQVJSQU5USUVTIE9SIENPTkRJVElPTlMgT0YgQU5ZIEtJTkQsIGVpdGhlciBleHByZXNzIG9yIGltcGxpZWQuIFNlZSB0aGVcbiAqIExpY2Vuc2UgZm9yIHRoZSBzcGVjaWZpYyBsYW5ndWFnZSBnb3Zlcm5pbmcgcGVybWlzc2lvbnMgYW5kIGxpbWl0YXRpb25zIHVuZGVyXG4gKiB0aGUgTGljZW5zZS5cbiAqL1xuXG4vKipcbiAqIFRoaXMgY29udGFpbnMgZnVuY3Rpb25zIGFuZCBmZWF0dXJlcyB1c2VkIHRvIGNvcHkgdGhlbWUgZmlsZXMuXG4gKi9cblxuaW1wb3J0IHsgcmVhZGRpclN5bmMsIHN0YXRTeW5jLCBta2RpclN5bmMsIGV4aXN0c1N5bmMsIGNvcHlGaWxlU3luYyB9IGZyb20gJ2ZzJztcbmltcG9ydCB7IHJlc29sdmUsIGJhc2VuYW1lLCByZWxhdGl2ZSwgZXh0bmFtZSB9IGZyb20gJ3BhdGgnO1xuaW1wb3J0IHsgZ2xvYlN5bmMgfSBmcm9tICdnbG9iJztcblxuY29uc3QgaWdub3JlZEZpbGVFeHRlbnNpb25zID0gWycuY3NzJywgJy5qcycsICcuanNvbiddO1xuXG4vKipcbiAqIENvcHkgdGhlbWUgc3RhdGljIHJlc291cmNlcyB0byBzdGF0aWMgYXNzZXRzIGZvbGRlci4gQWxsIGZpbGVzIGluIHRoZSB0aGVtZVxuICogZm9sZGVyIHdpbGwgYmUgY29waWVkIGV4Y2x1ZGluZyBjc3MsIGpzIGFuZCBqc29uIGZpbGVzIHRoYXQgd2lsbCBiZVxuICogaGFuZGxlZCBieSB3ZWJwYWNrIGFuZCBub3QgYmUgc2hhcmVkIGFzIHN0YXRpYyBmaWxlcy5cbiAqXG4gKiBAcGFyYW0ge3N0cmluZ30gdGhlbWVGb2xkZXIgRm9sZGVyIHdpdGggdGhlbWUgZmlsZVxuICogQHBhcmFtIHtzdHJpbmd9IHByb2plY3RTdGF0aWNBc3NldHNPdXRwdXRGb2xkZXIgcmVzb3VyY2VzIG91dHB1dCBmb2xkZXJcbiAqIEBwYXJhbSB7b2JqZWN0fSBsb2dnZXIgcGx1Z2luIGxvZ2dlclxuICovXG5mdW5jdGlvbiBjb3B5VGhlbWVSZXNvdXJjZXModGhlbWVGb2xkZXIsIHByb2plY3RTdGF0aWNBc3NldHNPdXRwdXRGb2xkZXIsIGxvZ2dlcikge1xuICBjb25zdCBzdGF0aWNBc3NldHNUaGVtZUZvbGRlciA9IHJlc29sdmUocHJvamVjdFN0YXRpY0Fzc2V0c091dHB1dEZvbGRlciwgJ3RoZW1lcycsIGJhc2VuYW1lKHRoZW1lRm9sZGVyKSk7XG4gIGNvbnN0IGNvbGxlY3Rpb24gPSBjb2xsZWN0Rm9sZGVycyh0aGVtZUZvbGRlciwgbG9nZ2VyKTtcblxuICAvLyBPbmx5IGNyZWF0ZSBhc3NldHMgZm9sZGVyIGlmIHRoZXJlIGFyZSBmaWxlcyB0byBjb3B5LlxuICBpZiAoY29sbGVjdGlvbi5maWxlcy5sZW5ndGggPiAwKSB7XG4gICAgbWtkaXJTeW5jKHN0YXRpY0Fzc2V0c1RoZW1lRm9sZGVyLCB7IHJlY3Vyc2l2ZTogdHJ1ZSB9KTtcbiAgICAvLyBjcmVhdGUgZm9sZGVycyB3aXRoXG4gICAgY29sbGVjdGlvbi5kaXJlY3Rvcmllcy5mb3JFYWNoKChkaXJlY3RvcnkpID0+IHtcbiAgICAgIGNvbnN0IHJlbGF0aXZlRGlyZWN0b3J5ID0gcmVsYXRpdmUodGhlbWVGb2xkZXIsIGRpcmVjdG9yeSk7XG4gICAgICBjb25zdCB0YXJnZXREaXJlY3RvcnkgPSByZXNvbHZlKHN0YXRpY0Fzc2V0c1RoZW1lRm9sZGVyLCByZWxhdGl2ZURpcmVjdG9yeSk7XG5cbiAgICAgIG1rZGlyU3luYyh0YXJnZXREaXJlY3RvcnksIHsgcmVjdXJzaXZlOiB0cnVlIH0pO1xuICAgIH0pO1xuXG4gICAgY29sbGVjdGlvbi5maWxlcy5mb3JFYWNoKChmaWxlKSA9PiB7XG4gICAgICBjb25zdCByZWxhdGl2ZUZpbGUgPSByZWxhdGl2ZSh0aGVtZUZvbGRlciwgZmlsZSk7XG4gICAgICBjb25zdCB0YXJnZXRGaWxlID0gcmVzb2x2ZShzdGF0aWNBc3NldHNUaGVtZUZvbGRlciwgcmVsYXRpdmVGaWxlKTtcbiAgICAgIGNvcHlGaWxlSWZBYnNlbnRPck5ld2VyKGZpbGUsIHRhcmdldEZpbGUsIGxvZ2dlcik7XG4gICAgfSk7XG4gIH1cbn1cblxuLyoqXG4gKiBDb2xsZWN0IGFsbCBmb2xkZXJzIHdpdGggY29weWFibGUgZmlsZXMgYW5kIGFsbCBmaWxlcyB0byBiZSBjb3BpZWQuXG4gKiBGb2xlZCB3aWxsIG5vdCBiZSBhZGRlZCBpZiBubyBmaWxlcyBpbiBmb2xkZXIgb3Igc3ViZm9sZGVycy5cbiAqXG4gKiBGaWxlcyB3aWxsIG5vdCBjb250YWluIGZpbGVzIHdpdGggaWdub3JlZCBleHRlbnNpb25zIGFuZCBmb2xkZXJzIG9ubHkgY29udGFpbmluZyBpZ25vcmVkIGZpbGVzIHdpbGwgbm90IGJlIGFkZGVkLlxuICpcbiAqIEBwYXJhbSBmb2xkZXJUb0NvcHkgZm9sZGVyIHdlIHdpbGwgY29weSBmaWxlcyBmcm9tXG4gKiBAcGFyYW0gbG9nZ2VyIHBsdWdpbiBsb2dnZXJcbiAqIEByZXR1cm4ge3tkaXJlY3RvcmllczogW10sIGZpbGVzOiBbXX19IG9iamVjdCBjb250YWluaW5nIGRpcmVjdG9yaWVzIHRvIGNyZWF0ZSBhbmQgZmlsZXMgdG8gY29weVxuICovXG5mdW5jdGlvbiBjb2xsZWN0Rm9sZGVycyhmb2xkZXJUb0NvcHksIGxvZ2dlcikge1xuICBjb25zdCBjb2xsZWN0aW9uID0geyBkaXJlY3RvcmllczogW10sIGZpbGVzOiBbXSB9O1xuICBsb2dnZXIudHJhY2UoJ2ZpbGVzIGluIGRpcmVjdG9yeScsIHJlYWRkaXJTeW5jKGZvbGRlclRvQ29weSkpO1xuICByZWFkZGlyU3luYyhmb2xkZXJUb0NvcHkpLmZvckVhY2goKGZpbGUpID0+IHtcbiAgICBjb25zdCBmaWxlVG9Db3B5ID0gcmVzb2x2ZShmb2xkZXJUb0NvcHksIGZpbGUpO1xuICAgIHRyeSB7XG4gICAgICBpZiAoc3RhdFN5bmMoZmlsZVRvQ29weSkuaXNEaXJlY3RvcnkoKSkge1xuICAgICAgICBsb2dnZXIuZGVidWcoJ0dvaW5nIHRocm91Z2ggZGlyZWN0b3J5JywgZmlsZVRvQ29weSk7XG4gICAgICAgIGNvbnN0IHJlc3VsdCA9IGNvbGxlY3RGb2xkZXJzKGZpbGVUb0NvcHksIGxvZ2dlcik7XG4gICAgICAgIGlmIChyZXN1bHQuZmlsZXMubGVuZ3RoID4gMCkge1xuICAgICAgICAgIGNvbGxlY3Rpb24uZGlyZWN0b3JpZXMucHVzaChmaWxlVG9Db3B5KTtcbiAgICAgICAgICBsb2dnZXIuZGVidWcoJ0FkZGluZyBkaXJlY3RvcnknLCBmaWxlVG9Db3B5KTtcbiAgICAgICAgICBjb2xsZWN0aW9uLmRpcmVjdG9yaWVzLnB1c2guYXBwbHkoY29sbGVjdGlvbi5kaXJlY3RvcmllcywgcmVzdWx0LmRpcmVjdG9yaWVzKTtcbiAgICAgICAgICBjb2xsZWN0aW9uLmZpbGVzLnB1c2guYXBwbHkoY29sbGVjdGlvbi5maWxlcywgcmVzdWx0LmZpbGVzKTtcbiAgICAgICAgfVxuICAgICAgfSBlbHNlIGlmICghaWdub3JlZEZpbGVFeHRlbnNpb25zLmluY2x1ZGVzKGV4dG5hbWUoZmlsZVRvQ29weSkpKSB7XG4gICAgICAgIGxvZ2dlci5kZWJ1ZygnQWRkaW5nIGZpbGUnLCBmaWxlVG9Db3B5KTtcbiAgICAgICAgY29sbGVjdGlvbi5maWxlcy5wdXNoKGZpbGVUb0NvcHkpO1xuICAgICAgfVxuICAgIH0gY2F0Y2ggKGVycm9yKSB7XG4gICAgICBoYW5kbGVOb1N1Y2hGaWxlRXJyb3IoZmlsZVRvQ29weSwgZXJyb3IsIGxvZ2dlcik7XG4gICAgfVxuICB9KTtcbiAgcmV0dXJuIGNvbGxlY3Rpb247XG59XG5cbi8qKlxuICogQ29weSBhbnkgc3RhdGljIG5vZGVfbW9kdWxlcyBhc3NldHMgbWFya2VkIGluIHRoZW1lLmpzb24gdG9cbiAqIHByb2plY3Qgc3RhdGljIGFzc2V0cyBmb2xkZXIuXG4gKlxuICogVGhlIHRoZW1lLmpzb24gY29udGVudCBmb3IgYXNzZXRzIGlzIHNldCB1cCBhczpcbiAqIHtcbiAqICAgYXNzZXRzOiB7XG4gKiAgICAgXCJub2RlX21vZHVsZSBpZGVudGlmaWVyXCI6IHtcbiAqICAgICAgIFwiY29weS1ydWxlXCI6IFwidGFyZ2V0L2ZvbGRlclwiLFxuICogICAgIH1cbiAqICAgfVxuICogfVxuICpcbiAqIFRoaXMgd291bGQgbWVhbiB0aGF0IGFuIGFzc2V0IHdvdWxkIGJlIGJ1aWx0IGFzOlxuICogXCJAZm9ydGF3ZXNvbWUvZm9udGF3ZXNvbWUtZnJlZVwiOiB7XG4gKiAgIFwic3Zncy9yZWd1bGFyLyoqXCI6IFwiZm9ydGF3ZXNvbWUvaWNvbnNcIlxuICogfVxuICogV2hlcmUgJ0Bmb3J0YXdlc29tZS9mb250YXdlc29tZS1mcmVlJyBpcyB0aGUgbnBtIHBhY2thZ2UsICdzdmdzL3JlZ3VsYXIvKionIGlzIHdoYXQgc2hvdWxkIGJlIGNvcGllZFxuICogYW5kICdmb3J0YXdlc29tZS9pY29ucycgaXMgdGhlIHRhcmdldCBkaXJlY3RvcnkgdW5kZXIgcHJvamVjdFN0YXRpY0Fzc2V0c091dHB1dEZvbGRlciB3aGVyZSB0aGluZ3NcbiAqIHdpbGwgZ2V0IGNvcGllZCB0by5cbiAqXG4gKiBOb3RlISB0aGVyZSBjYW4gYmUgbXVsdGlwbGUgY29weS1ydWxlcyB3aXRoIHRhcmdldCBmb2xkZXJzIGZvciBvbmUgbnBtIHBhY2thZ2UgYXNzZXQuXG4gKlxuICogQHBhcmFtIHtzdHJpbmd9IHRoZW1lTmFtZSBuYW1lIG9mIHRoZSB0aGVtZSB3ZSBhcmUgY29weWluZyBhc3NldHMgZm9yXG4gKiBAcGFyYW0ge2pzb259IHRoZW1lUHJvcGVydGllcyB0aGVtZSBwcm9wZXJ0aWVzIGpzb24gd2l0aCBkYXRhIG9uIGFzc2V0c1xuICogQHBhcmFtIHtzdHJpbmd9IHByb2plY3RTdGF0aWNBc3NldHNPdXRwdXRGb2xkZXIgcHJvamVjdCBvdXRwdXQgZm9sZGVyIHdoZXJlIHdlIGNvcHkgYXNzZXRzIHRvIHVuZGVyIHRoZW1lL1t0aGVtZU5hbWVdXG4gKiBAcGFyYW0ge29iamVjdH0gbG9nZ2VyIHBsdWdpbiBsb2dnZXJcbiAqL1xuZnVuY3Rpb24gY29weVN0YXRpY0Fzc2V0cyh0aGVtZU5hbWUsIHRoZW1lUHJvcGVydGllcywgcHJvamVjdFN0YXRpY0Fzc2V0c091dHB1dEZvbGRlciwgbG9nZ2VyKSB7XG4gIGNvbnN0IGFzc2V0cyA9IHRoZW1lUHJvcGVydGllc1snYXNzZXRzJ107XG4gIGlmICghYXNzZXRzKSB7XG4gICAgbG9nZ2VyLmRlYnVnKCdubyBhc3NldHMgdG8gaGFuZGxlIG5vIHN0YXRpYyBhc3NldHMgd2VyZSBjb3BpZWQnKTtcbiAgICByZXR1cm47XG4gIH1cblxuICBta2RpclN5bmMocHJvamVjdFN0YXRpY0Fzc2V0c091dHB1dEZvbGRlciwge1xuICAgIHJlY3Vyc2l2ZTogdHJ1ZVxuICB9KTtcbiAgY29uc3QgbWlzc2luZ01vZHVsZXMgPSBjaGVja01vZHVsZXMoT2JqZWN0LmtleXMoYXNzZXRzKSk7XG4gIGlmIChtaXNzaW5nTW9kdWxlcy5sZW5ndGggPiAwKSB7XG4gICAgdGhyb3cgRXJyb3IoXG4gICAgICBcIk1pc3NpbmcgbnBtIG1vZHVsZXMgJ1wiICtcbiAgICAgICAgbWlzc2luZ01vZHVsZXMuam9pbihcIicsICdcIikgK1xuICAgICAgICBcIicgZm9yIGFzc2V0cyBtYXJrZWQgaW4gJ3RoZW1lLmpzb24nLlxcblwiICtcbiAgICAgICAgXCJJbnN0YWxsIHBhY2thZ2UocykgYnkgYWRkaW5nIGEgQE5wbVBhY2thZ2UgYW5ub3RhdGlvbiBvciBpbnN0YWxsIGl0IHVzaW5nICducG0vcG5wbS9idW4gaSdcIlxuICAgICk7XG4gIH1cbiAgT2JqZWN0LmtleXMoYXNzZXRzKS5mb3JFYWNoKChtb2R1bGUpID0+IHtcbiAgICBjb25zdCBjb3B5UnVsZXMgPSBhc3NldHNbbW9kdWxlXTtcbiAgICBPYmplY3Qua2V5cyhjb3B5UnVsZXMpLmZvckVhY2goKGNvcHlSdWxlKSA9PiB7XG4gICAgICBjb25zdCBub2RlU291cmNlcyA9IHJlc29sdmUoJ25vZGVfbW9kdWxlcy8nLCBtb2R1bGUsIGNvcHlSdWxlKTtcbiAgICAgIGNvbnN0IGZpbGVzID0gZ2xvYlN5bmMobm9kZVNvdXJjZXMsIHsgbm9kaXI6IHRydWUgfSk7XG4gICAgICBjb25zdCB0YXJnZXRGb2xkZXIgPSByZXNvbHZlKHByb2plY3RTdGF0aWNBc3NldHNPdXRwdXRGb2xkZXIsICd0aGVtZXMnLCB0aGVtZU5hbWUsIGNvcHlSdWxlc1tjb3B5UnVsZV0pO1xuXG4gICAgICBta2RpclN5bmModGFyZ2V0Rm9sZGVyLCB7XG4gICAgICAgIHJlY3Vyc2l2ZTogdHJ1ZVxuICAgICAgfSk7XG4gICAgICBmaWxlcy5mb3JFYWNoKChmaWxlKSA9PiB7XG4gICAgICAgIGNvbnN0IGNvcHlUYXJnZXQgPSByZXNvbHZlKHRhcmdldEZvbGRlciwgYmFzZW5hbWUoZmlsZSkpO1xuICAgICAgICBjb3B5RmlsZUlmQWJzZW50T3JOZXdlcihmaWxlLCBjb3B5VGFyZ2V0LCBsb2dnZXIpO1xuICAgICAgfSk7XG4gICAgfSk7XG4gIH0pO1xufVxuXG5mdW5jdGlvbiBjaGVja01vZHVsZXMobW9kdWxlcykge1xuICBjb25zdCBtaXNzaW5nID0gW107XG5cbiAgbW9kdWxlcy5mb3JFYWNoKChtb2R1bGUpID0+IHtcbiAgICBpZiAoIWV4aXN0c1N5bmMocmVzb2x2ZSgnbm9kZV9tb2R1bGVzLycsIG1vZHVsZSkpKSB7XG4gICAgICBtaXNzaW5nLnB1c2gobW9kdWxlKTtcbiAgICB9XG4gIH0pO1xuXG4gIHJldHVybiBtaXNzaW5nO1xufVxuXG4vKipcbiAqIENvcGllcyBnaXZlbiBmaWxlIHRvIGEgZ2l2ZW4gdGFyZ2V0IHBhdGgsIGlmIHRhcmdldCBmaWxlIGRvZXNuJ3QgZXhpc3Qgb3IgaWZcbiAqIGZpbGUgdG8gY29weSBpcyBuZXdlci5cbiAqIEBwYXJhbSB7c3RyaW5nfSBmaWxlVG9Db3B5IHBhdGggb2YgdGhlIGZpbGUgdG8gY29weVxuICogQHBhcmFtIHtzdHJpbmd9IGNvcHlUYXJnZXQgcGF0aCBvZiB0aGUgdGFyZ2V0IGZpbGVcbiAqIEBwYXJhbSB7b2JqZWN0fSBsb2dnZXIgcGx1Z2luIGxvZ2dlclxuICovXG5mdW5jdGlvbiBjb3B5RmlsZUlmQWJzZW50T3JOZXdlcihmaWxlVG9Db3B5LCBjb3B5VGFyZ2V0LCBsb2dnZXIpIHtcbiAgdHJ5IHtcbiAgICBpZiAoIWV4aXN0c1N5bmMoY29weVRhcmdldCkgfHwgc3RhdFN5bmMoY29weVRhcmdldCkubXRpbWUgPCBzdGF0U3luYyhmaWxlVG9Db3B5KS5tdGltZSkge1xuICAgICAgbG9nZ2VyLnRyYWNlKCdDb3B5aW5nOiAnLCBmaWxlVG9Db3B5LCAnPT4nLCBjb3B5VGFyZ2V0KTtcbiAgICAgIGNvcHlGaWxlU3luYyhmaWxlVG9Db3B5LCBjb3B5VGFyZ2V0KTtcbiAgICB9XG4gIH0gY2F0Y2ggKGVycm9yKSB7XG4gICAgaGFuZGxlTm9TdWNoRmlsZUVycm9yKGZpbGVUb0NvcHksIGVycm9yLCBsb2dnZXIpO1xuICB9XG59XG5cbi8vIElnbm9yZXMgZXJyb3JzIGR1ZSB0byBmaWxlIG1pc3NpbmcgZHVyaW5nIHRoZW1lIHByb2Nlc3Npbmdcbi8vIFRoaXMgbWF5IGhhcHBlbiBmb3IgZXhhbXBsZSB3aGVuIGFuIElERSBjcmVhdGVzIGEgdGVtcG9yYXJ5IGZpbGVcbi8vIGFuZCB0aGVuIGltbWVkaWF0ZWx5IGRlbGV0ZXMgaXRcbmZ1bmN0aW9uIGhhbmRsZU5vU3VjaEZpbGVFcnJvcihmaWxlLCBlcnJvciwgbG9nZ2VyKSB7XG4gIGlmIChlcnJvci5jb2RlID09PSAnRU5PRU5UJykge1xuICAgIGxvZ2dlci53YXJuKCdJZ25vcmluZyBub3QgZXhpc3RpbmcgZmlsZSAnICsgZmlsZSArICcuIEZpbGUgbWF5IGhhdmUgYmVlbiBkZWxldGVkIGR1cmluZyB0aGVtZSBwcm9jZXNzaW5nLicpO1xuICB9IGVsc2Uge1xuICAgIHRocm93IGVycm9yO1xuICB9XG59XG5cbmV4cG9ydCB7IGNoZWNrTW9kdWxlcywgY29weVN0YXRpY0Fzc2V0cywgY29weVRoZW1lUmVzb3VyY2VzIH07XG4iLCAiY29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2Rpcm5hbWUgPSBcIkY6XFxcXFByb3llY3RvLVNlZ3VyaWRhZFxcXFxnZXN0aW9uLXNlZ3VyaWRhZC1hZXJvcG9ydHVhcmlhXFxcXHRhcmdldFxcXFxwbHVnaW5zXFxcXHRoZW1lLWxvYWRlclwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiRjpcXFxcUHJveWVjdG8tU2VndXJpZGFkXFxcXGdlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWFcXFxcdGFyZ2V0XFxcXHBsdWdpbnNcXFxcdGhlbWUtbG9hZGVyXFxcXHRoZW1lLWxvYWRlci11dGlscy5qc1wiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9pbXBvcnRfbWV0YV91cmwgPSBcImZpbGU6Ly8vRjovUHJveWVjdG8tU2VndXJpZGFkL2dlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWEvdGFyZ2V0L3BsdWdpbnMvdGhlbWUtbG9hZGVyL3RoZW1lLWxvYWRlci11dGlscy5qc1wiO2ltcG9ydCB7IGV4aXN0c1N5bmMsIHJlYWRGaWxlU3luYyB9IGZyb20gJ2ZzJztcbmltcG9ydCB7IHJlc29sdmUsIGJhc2VuYW1lIH0gZnJvbSAncGF0aCc7XG5pbXBvcnQgeyBnbG9iU3luYyB9IGZyb20gJ2dsb2InO1xuXG4vLyBDb2xsZWN0IGdyb3VwcyBbdXJsKF0gWyd8XCJdb3B0aW9uYWwgJy4vfC4uLycsIG90aGVyICcuLi8nIHNlZ21lbnRzIG9wdGlvbmFsLCBmaWxlIHBhcnQgYW5kIGVuZCBvZiB1cmxcbi8vIFRoZSBhZGRpdGlvbmFsIGRvdCBzZWdtZW50cyBjb3VsZCBiZSBVUkwgcmVmZXJlbmNpbmcgYXNzZXRzIGluIG5lc3RlZCBpbXBvcnRlZCBDU1Ncbi8vIFdoZW4gVml0ZSBpbmxpbmVzIENTUyBpbXBvcnQgaXQgZG9lcyBub3QgcmV3cml0ZSByZWxhdGl2ZSBVUkwgZm9yIG5vdC1yZXNvbHZhYmxlIHJlc291cmNlXG4vLyBzbyB0aGUgZmluYWwgQ1NTIGVuZHMgdXAgd2l0aCB3cm9uZyByZWxhdGl2ZSBVUkxzIChyLmcuIC4uLy4uL3BrZy9pY29uLnN2Zylcbi8vIElmIHRoZSBVUkwgaXMgcmVsYXRpdmUsIHdlIHNob3VsZCB0cnkgdG8gY2hlY2sgaWYgaXQgaXMgYW4gYXNzZXQgYnkgaWdub3JpbmcgdGhlIGFkZGl0aW9uYWwgZG90IHNlZ21lbnRzXG5jb25zdCB1cmxNYXRjaGVyID0gLyh1cmxcXChcXHMqKShcXCd8XFxcIik/KFxcLlxcL3xcXC5cXC5cXC8pKCg/OlxcMykqKT8oXFxTKikoXFwyXFxzKlxcKSkvZztcblxuZnVuY3Rpb24gYXNzZXRzQ29udGFpbnMoZmlsZVVybCwgdGhlbWVGb2xkZXIsIGxvZ2dlcikge1xuICBjb25zdCB0aGVtZVByb3BlcnRpZXMgPSBnZXRUaGVtZVByb3BlcnRpZXModGhlbWVGb2xkZXIpO1xuICBpZiAoIXRoZW1lUHJvcGVydGllcykge1xuICAgIGxvZ2dlci5kZWJ1ZygnTm8gdGhlbWUgcHJvcGVydGllcyBmb3VuZC4nKTtcbiAgICByZXR1cm4gZmFsc2U7XG4gIH1cbiAgY29uc3QgYXNzZXRzID0gdGhlbWVQcm9wZXJ0aWVzWydhc3NldHMnXTtcbiAgaWYgKCFhc3NldHMpIHtcbiAgICBsb2dnZXIuZGVidWcoJ05vIGRlZmluZWQgYXNzZXRzIGluIHRoZW1lIHByb3BlcnRpZXMnKTtcbiAgICByZXR1cm4gZmFsc2U7XG4gIH1cbiAgLy8gR28gdGhyb3VnaCBlYWNoIGFzc2V0IG1vZHVsZVxuICBmb3IgKGxldCBtb2R1bGUgb2YgT2JqZWN0LmtleXMoYXNzZXRzKSkge1xuICAgIGNvbnN0IGNvcHlSdWxlcyA9IGFzc2V0c1ttb2R1bGVdO1xuICAgIC8vIEdvIHRocm91Z2ggZWFjaCBjb3B5IHJ1bGVcbiAgICBmb3IgKGxldCBjb3B5UnVsZSBvZiBPYmplY3Qua2V5cyhjb3B5UnVsZXMpKSB7XG4gICAgICAvLyBpZiBmaWxlIHN0YXJ0cyB3aXRoIGNvcHlSdWxlIHRhcmdldCBjaGVjayBpZiBmaWxlIHdpdGggcGF0aCBhZnRlciBjb3B5IHRhcmdldCBjYW4gYmUgZm91bmRcbiAgICAgIGlmIChmaWxlVXJsLnN0YXJ0c1dpdGgoY29weVJ1bGVzW2NvcHlSdWxlXSkpIHtcbiAgICAgICAgY29uc3QgdGFyZ2V0RmlsZSA9IGZpbGVVcmwucmVwbGFjZShjb3B5UnVsZXNbY29weVJ1bGVdLCAnJyk7XG4gICAgICAgIGNvbnN0IGZpbGVzID0gZ2xvYlN5bmMocmVzb2x2ZSgnbm9kZV9tb2R1bGVzLycsIG1vZHVsZSwgY29weVJ1bGUpLCB7IG5vZGlyOiB0cnVlIH0pO1xuXG4gICAgICAgIGZvciAobGV0IGZpbGUgb2YgZmlsZXMpIHtcbiAgICAgICAgICBpZiAoZmlsZS5lbmRzV2l0aCh0YXJnZXRGaWxlKSkgcmV0dXJuIHRydWU7XG4gICAgICAgIH1cbiAgICAgIH1cbiAgICB9XG4gIH1cbiAgcmV0dXJuIGZhbHNlO1xufVxuXG5mdW5jdGlvbiBnZXRUaGVtZVByb3BlcnRpZXModGhlbWVGb2xkZXIpIHtcbiAgY29uc3QgdGhlbWVQcm9wZXJ0eUZpbGUgPSByZXNvbHZlKHRoZW1lRm9sZGVyLCAndGhlbWUuanNvbicpO1xuICBpZiAoIWV4aXN0c1N5bmModGhlbWVQcm9wZXJ0eUZpbGUpKSB7XG4gICAgcmV0dXJuIHt9O1xuICB9XG4gIGNvbnN0IHRoZW1lUHJvcGVydHlGaWxlQXNTdHJpbmcgPSByZWFkRmlsZVN5bmModGhlbWVQcm9wZXJ0eUZpbGUpO1xuICBpZiAodGhlbWVQcm9wZXJ0eUZpbGVBc1N0cmluZy5sZW5ndGggPT09IDApIHtcbiAgICByZXR1cm4ge307XG4gIH1cbiAgcmV0dXJuIEpTT04ucGFyc2UodGhlbWVQcm9wZXJ0eUZpbGVBc1N0cmluZyk7XG59XG5cbmZ1bmN0aW9uIHJld3JpdGVDc3NVcmxzKHNvdXJjZSwgaGFuZGxlZFJlc291cmNlRm9sZGVyLCB0aGVtZUZvbGRlciwgbG9nZ2VyLCBvcHRpb25zKSB7XG4gIHNvdXJjZSA9IHNvdXJjZS5yZXBsYWNlKHVybE1hdGNoZXIsIGZ1bmN0aW9uIChtYXRjaCwgdXJsLCBxdW90ZU1hcmssIHJlcGxhY2UsIGFkZGl0aW9uYWxEb3RTZWdtZW50cywgZmlsZVVybCwgZW5kU3RyaW5nKSB7XG4gICAgbGV0IGFic29sdXRlUGF0aCA9IHJlc29sdmUoaGFuZGxlZFJlc291cmNlRm9sZGVyLCByZXBsYWNlLCBhZGRpdGlvbmFsRG90U2VnbWVudHMgfHwgJycsIGZpbGVVcmwpO1xuICAgIGxldCBleGlzdGluZ1RoZW1lUmVzb3VyY2UgPSBhYnNvbHV0ZVBhdGguc3RhcnRzV2l0aCh0aGVtZUZvbGRlcikgJiYgZXhpc3RzU3luYyhhYnNvbHV0ZVBhdGgpO1xuICAgIGlmICghZXhpc3RpbmdUaGVtZVJlc291cmNlICYmIGFkZGl0aW9uYWxEb3RTZWdtZW50cykge1xuICAgICAgLy8gVHJ5IHRvIHJlc29sdmUgcGF0aCB3aXRob3V0IGRvdCBzZWdtZW50cyBhcyBpdCBtYXkgYmUgYW4gdW5yZXNvbHZhYmxlXG4gICAgICAvLyByZWxhdGl2ZSBVUkwgZnJvbSBhbiBpbmxpbmVkIG5lc3RlZCBDU1NcbiAgICAgIGFic29sdXRlUGF0aCA9IHJlc29sdmUoaGFuZGxlZFJlc291cmNlRm9sZGVyLCByZXBsYWNlLCBmaWxlVXJsKTtcbiAgICAgIGV4aXN0aW5nVGhlbWVSZXNvdXJjZSA9IGFic29sdXRlUGF0aC5zdGFydHNXaXRoKHRoZW1lRm9sZGVyKSAmJiBleGlzdHNTeW5jKGFic29sdXRlUGF0aCk7XG4gICAgfVxuICAgIGNvbnN0IGlzQXNzZXQgPSBhc3NldHNDb250YWlucyhmaWxlVXJsLCB0aGVtZUZvbGRlciwgbG9nZ2VyKTtcbiAgICBpZiAoZXhpc3RpbmdUaGVtZVJlc291cmNlIHx8IGlzQXNzZXQpIHtcbiAgICAgIC8vIEFkZGluZyAuLyB3aWxsIHNraXAgY3NzLWxvYWRlciwgd2hpY2ggc2hvdWxkIGJlIGRvbmUgZm9yIGFzc2V0IGZpbGVzXG4gICAgICAvLyBJbiBhIHByb2R1Y3Rpb24gYnVpbGQsIHRoZSBjc3MgZmlsZSBpcyBpbiBWQUFESU4vYnVpbGQgYW5kIHN0YXRpYyBmaWxlcyBhcmUgaW4gVkFBRElOL3N0YXRpYywgc28gLi4vc3RhdGljIG5lZWRzIHRvIGJlIGFkZGVkXG4gICAgICBjb25zdCByZXBsYWNlbWVudCA9IG9wdGlvbnMuZGV2TW9kZSA/ICcuLycgOiAnLi4vc3RhdGljLyc7XG5cbiAgICAgIGNvbnN0IHNraXBMb2FkZXIgPSBleGlzdGluZ1RoZW1lUmVzb3VyY2UgPyAnJyA6IHJlcGxhY2VtZW50O1xuICAgICAgY29uc3QgZnJvbnRlbmRUaGVtZUZvbGRlciA9IHNraXBMb2FkZXIgKyAndGhlbWVzLycgKyBiYXNlbmFtZSh0aGVtZUZvbGRlcik7XG4gICAgICBsb2dnZXIubG9nKFxuICAgICAgICAnVXBkYXRpbmcgdXJsIGZvciBmaWxlJyxcbiAgICAgICAgXCInXCIgKyByZXBsYWNlICsgZmlsZVVybCArIFwiJ1wiLFxuICAgICAgICAndG8gdXNlJyxcbiAgICAgICAgXCInXCIgKyBmcm9udGVuZFRoZW1lRm9sZGVyICsgJy8nICsgZmlsZVVybCArIFwiJ1wiXG4gICAgICApO1xuICAgICAgLy8gYXNzZXRzIGFyZSBhbHdheXMgcmVsYXRpdmUgdG8gdGhlbWUgZm9sZGVyXG4gICAgICBjb25zdCBwYXRoUmVzb2x2ZWQgPSBpc0Fzc2V0ID8gJy8nICsgZmlsZVVybFxuICAgICAgICAgIDogYWJzb2x1dGVQYXRoLnN1YnN0cmluZyh0aGVtZUZvbGRlci5sZW5ndGgpLnJlcGxhY2UoL1xcXFwvZywgJy8nKTtcblxuICAgICAgLy8ga2VlcCB0aGUgdXJsIHRoZSBzYW1lIGV4Y2VwdCByZXBsYWNlIHRoZSAuLyBvciAuLi8gdG8gdGhlbWVzL1t0aGVtZUZvbGRlcl1cbiAgICAgIHJldHVybiB1cmwgKyAocXVvdGVNYXJrID8/ICcnKSArIGZyb250ZW5kVGhlbWVGb2xkZXIgKyBwYXRoUmVzb2x2ZWQgKyBlbmRTdHJpbmc7XG4gICAgfSBlbHNlIGlmIChvcHRpb25zLmRldk1vZGUpIHtcbiAgICAgIGxvZ2dlci5sb2coXCJObyByZXdyaXRlIGZvciAnXCIsIG1hdGNoLCBcIicgYXMgdGhlIGZpbGUgd2FzIG5vdCBmb3VuZC5cIik7XG4gICAgfSBlbHNlIHtcbiAgICAgIC8vIEluIHByb2R1Y3Rpb24sIHRoZSBjc3MgaXMgaW4gVkFBRElOL2J1aWxkIGJ1dCB0aGUgdGhlbWUgZmlsZXMgYXJlIGluIC5cbiAgICAgIHJldHVybiB1cmwgKyAocXVvdGVNYXJrID8/ICcnKSArICcuLi8uLi8nICsgZmlsZVVybCArIGVuZFN0cmluZztcbiAgICB9XG4gICAgcmV0dXJuIG1hdGNoO1xuICB9KTtcbiAgcmV0dXJuIHNvdXJjZTtcbn1cblxuZXhwb3J0IHsgcmV3cml0ZUNzc1VybHMgfTtcbiIsICJjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfZGlybmFtZSA9IFwiRjpcXFxcUHJveWVjdG8tU2VndXJpZGFkXFxcXGdlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWFcXFxcdGFyZ2V0XFxcXHBsdWdpbnNcXFxccmVhY3QtZnVuY3Rpb24tbG9jYXRpb24tcGx1Z2luXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ZpbGVuYW1lID0gXCJGOlxcXFxQcm95ZWN0by1TZWd1cmlkYWRcXFxcZ2VzdGlvbi1zZWd1cmlkYWQtYWVyb3BvcnR1YXJpYVxcXFx0YXJnZXRcXFxccGx1Z2luc1xcXFxyZWFjdC1mdW5jdGlvbi1sb2NhdGlvbi1wbHVnaW5cXFxccmVhY3QtZnVuY3Rpb24tbG9jYXRpb24tcGx1Z2luLmpzXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ltcG9ydF9tZXRhX3VybCA9IFwiZmlsZTovLy9GOi9Qcm95ZWN0by1TZWd1cmlkYWQvZ2VzdGlvbi1zZWd1cmlkYWQtYWVyb3BvcnR1YXJpYS90YXJnZXQvcGx1Z2lucy9yZWFjdC1mdW5jdGlvbi1sb2NhdGlvbi1wbHVnaW4vcmVhY3QtZnVuY3Rpb24tbG9jYXRpb24tcGx1Z2luLmpzXCI7aW1wb3J0ICogYXMgdCBmcm9tICdAYmFiZWwvdHlwZXMnO1xuXG5leHBvcnQgZnVuY3Rpb24gYWRkRnVuY3Rpb25Db21wb25lbnRTb3VyY2VMb2NhdGlvbkJhYmVsKCkge1xuICBmdW5jdGlvbiBpc1JlYWN0RnVuY3Rpb25OYW1lKG5hbWUpIHtcbiAgICAvLyBBIFJlYWN0IGNvbXBvbmVudCBmdW5jdGlvbiBhbHdheXMgc3RhcnRzIHdpdGggYSBDYXBpdGFsIGxldHRlclxuICAgIHJldHVybiBuYW1lICYmIG5hbWUubWF0Y2goL15bQS1aXS4qLyk7XG4gIH1cblxuICAvKipcbiAgICogV3JpdGVzIGRlYnVnIGluZm8gYXMgTmFtZS5fX2RlYnVnU291cmNlRGVmaW5lPXsuLi59IGFmdGVyIHRoZSBnaXZlbiBzdGF0ZW1lbnQgKFwicGF0aFwiKS5cbiAgICogVGhpcyBpcyB1c2VkIHRvIG1ha2UgdGhlIHNvdXJjZSBsb2NhdGlvbiBvZiB0aGUgZnVuY3Rpb24gKGRlZmluZWQgYnkgdGhlIGxvYyBwYXJhbWV0ZXIpIGF2YWlsYWJsZSBpbiB0aGUgYnJvd3NlciBpbiBkZXZlbG9wbWVudCBtb2RlLlxuICAgKiBUaGUgbmFtZSBfX2RlYnVnU291cmNlRGVmaW5lIGlzIHByZWZpeGVkIGJ5IF9fIHRvIG1hcmsgdGhpcyBpcyBub3QgYSBwdWJsaWMgQVBJLlxuICAgKi9cbiAgZnVuY3Rpb24gYWRkRGVidWdJbmZvKHBhdGgsIG5hbWUsIGZpbGVuYW1lLCBsb2MpIHtcbiAgICBjb25zdCBsaW5lTnVtYmVyID0gbG9jLnN0YXJ0LmxpbmU7XG4gICAgY29uc3QgY29sdW1uTnVtYmVyID0gbG9jLnN0YXJ0LmNvbHVtbiArIDE7XG4gICAgY29uc3QgZGVidWdTb3VyY2VNZW1iZXIgPSB0Lm1lbWJlckV4cHJlc3Npb24odC5pZGVudGlmaWVyKG5hbWUpLCB0LmlkZW50aWZpZXIoJ19fZGVidWdTb3VyY2VEZWZpbmUnKSk7XG4gICAgY29uc3QgZGVidWdTb3VyY2VEZWZpbmUgPSB0Lm9iamVjdEV4cHJlc3Npb24oW1xuICAgICAgdC5vYmplY3RQcm9wZXJ0eSh0LmlkZW50aWZpZXIoJ2ZpbGVOYW1lJyksIHQuc3RyaW5nTGl0ZXJhbChmaWxlbmFtZSkpLFxuICAgICAgdC5vYmplY3RQcm9wZXJ0eSh0LmlkZW50aWZpZXIoJ2xpbmVOdW1iZXInKSwgdC5udW1lcmljTGl0ZXJhbChsaW5lTnVtYmVyKSksXG4gICAgICB0Lm9iamVjdFByb3BlcnR5KHQuaWRlbnRpZmllcignY29sdW1uTnVtYmVyJyksIHQubnVtZXJpY0xpdGVyYWwoY29sdW1uTnVtYmVyKSlcbiAgICBdKTtcbiAgICBjb25zdCBhc3NpZ25tZW50ID0gdC5leHByZXNzaW9uU3RhdGVtZW50KHQuYXNzaWdubWVudEV4cHJlc3Npb24oJz0nLCBkZWJ1Z1NvdXJjZU1lbWJlciwgZGVidWdTb3VyY2VEZWZpbmUpKTtcbiAgICBjb25zdCBjb25kaXRpb24gPSB0LmJpbmFyeUV4cHJlc3Npb24oXG4gICAgICAnPT09JyxcbiAgICAgIHQudW5hcnlFeHByZXNzaW9uKCd0eXBlb2YnLCB0LmlkZW50aWZpZXIobmFtZSkpLFxuICAgICAgdC5zdHJpbmdMaXRlcmFsKCdmdW5jdGlvbicpXG4gICAgKTtcbiAgICBjb25zdCBpZkZ1bmN0aW9uID0gdC5pZlN0YXRlbWVudChjb25kaXRpb24sIHQuYmxvY2tTdGF0ZW1lbnQoW2Fzc2lnbm1lbnRdKSk7XG4gICAgcGF0aC5pbnNlcnRBZnRlcihpZkZ1bmN0aW9uKTtcbiAgfVxuXG4gIHJldHVybiB7XG4gICAgdmlzaXRvcjoge1xuICAgICAgVmFyaWFibGVEZWNsYXJhdGlvbihwYXRoLCBzdGF0ZSkge1xuICAgICAgICAvLyBGaW5kcyBkZWNsYXJhdGlvbnMgc3VjaCBhc1xuICAgICAgICAvLyBjb25zdCBGb28gPSAoKSA9PiA8ZGl2Lz5cbiAgICAgICAgLy8gZXhwb3J0IGNvbnN0IEJhciA9ICgpID0+IDxzcGFuLz5cblxuICAgICAgICAvLyBhbmQgd3JpdGVzIGEgRm9vLl9fZGVidWdTb3VyY2VEZWZpbmU9IHsuLn0gYWZ0ZXIgaXQsIHJlZmVycmluZyB0byB0aGUgc3RhcnQgb2YgdGhlIGZ1bmN0aW9uIGJvZHlcbiAgICAgICAgcGF0aC5ub2RlLmRlY2xhcmF0aW9ucy5mb3JFYWNoKChkZWNsYXJhdGlvbikgPT4ge1xuICAgICAgICAgIGlmIChkZWNsYXJhdGlvbi5pZC50eXBlICE9PSAnSWRlbnRpZmllcicpIHtcbiAgICAgICAgICAgIHJldHVybjtcbiAgICAgICAgICB9XG4gICAgICAgICAgY29uc3QgbmFtZSA9IGRlY2xhcmF0aW9uPy5pZD8ubmFtZTtcbiAgICAgICAgICBpZiAoIWlzUmVhY3RGdW5jdGlvbk5hbWUobmFtZSkpIHtcbiAgICAgICAgICAgIHJldHVybjtcbiAgICAgICAgICB9XG5cbiAgICAgICAgICBjb25zdCBmaWxlbmFtZSA9IHN0YXRlLmZpbGUub3B0cy5maWxlbmFtZTtcbiAgICAgICAgICBpZiAoZGVjbGFyYXRpb24/LmluaXQ/LmJvZHk/LmxvYykge1xuICAgICAgICAgICAgYWRkRGVidWdJbmZvKHBhdGgsIG5hbWUsIGZpbGVuYW1lLCBkZWNsYXJhdGlvbi5pbml0LmJvZHkubG9jKTtcbiAgICAgICAgICB9XG4gICAgICAgIH0pO1xuICAgICAgfSxcblxuICAgICAgRnVuY3Rpb25EZWNsYXJhdGlvbihwYXRoLCBzdGF0ZSkge1xuICAgICAgICAvLyBGaW5kcyBkZWNsYXJhdGlvbnMgc3VjaCBhc1xuICAgICAgICAvLyBmdW5jdGlvIEZvbygpIHsgcmV0dXJuIDxkaXYvPjsgfVxuICAgICAgICAvLyBleHBvcnQgZnVuY3Rpb24gQmFyKCkgeyByZXR1cm4gPHNwYW4+SGVsbG88L3NwYW4+O31cblxuICAgICAgICAvLyBhbmQgd3JpdGVzIGEgRm9vLl9fZGVidWdTb3VyY2VEZWZpbmU9IHsuLn0gYWZ0ZXIgaXQsIHJlZmVycmluZyB0byB0aGUgc3RhcnQgb2YgdGhlIGZ1bmN0aW9uIGJvZHlcbiAgICAgICAgY29uc3Qgbm9kZSA9IHBhdGgubm9kZTtcbiAgICAgICAgY29uc3QgbmFtZSA9IG5vZGU/LmlkPy5uYW1lO1xuICAgICAgICBpZiAoIWlzUmVhY3RGdW5jdGlvbk5hbWUobmFtZSkpIHtcbiAgICAgICAgICByZXR1cm47XG4gICAgICAgIH1cbiAgICAgICAgY29uc3QgZmlsZW5hbWUgPSBzdGF0ZS5maWxlLm9wdHMuZmlsZW5hbWU7XG4gICAgICAgIGFkZERlYnVnSW5mbyhwYXRoLCBuYW1lLCBmaWxlbmFtZSwgbm9kZS5ib2R5LmxvYyk7XG4gICAgICB9XG4gICAgfVxuICB9O1xufVxuIiwgIntcbiAgXCJmcm9udGVuZEZvbGRlclwiOiBcIkY6L1Byb3llY3RvLVNlZ3VyaWRhZC9nZXN0aW9uLXNlZ3VyaWRhZC1hZXJvcG9ydHVhcmlhLy4vc3JjL21haW4vZnJvbnRlbmRcIixcbiAgXCJ0aGVtZUZvbGRlclwiOiBcInRoZW1lc1wiLFxuICBcInRoZW1lUmVzb3VyY2VGb2xkZXJcIjogXCJGOi9Qcm95ZWN0by1TZWd1cmlkYWQvZ2VzdGlvbi1zZWd1cmlkYWQtYWVyb3BvcnR1YXJpYS8uL3NyYy9tYWluL2Zyb250ZW5kL2dlbmVyYXRlZC9qYXItcmVzb3VyY2VzXCIsXG4gIFwic3RhdGljT3V0cHV0XCI6IFwiRjovUHJveWVjdG8tU2VndXJpZGFkL2dlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWEvdGFyZ2V0L2NsYXNzZXMvTUVUQS1JTkYvVkFBRElOL3dlYmFwcC9WQUFESU4vc3RhdGljXCIsXG4gIFwiZ2VuZXJhdGVkRm9sZGVyXCI6IFwiZ2VuZXJhdGVkXCIsXG4gIFwic3RhdHNPdXRwdXRcIjogXCJGOlxcXFxQcm95ZWN0by1TZWd1cmlkYWRcXFxcZ2VzdGlvbi1zZWd1cmlkYWQtYWVyb3BvcnR1YXJpYVxcXFx0YXJnZXRcXFxcY2xhc3Nlc1xcXFxNRVRBLUlORlxcXFxWQUFESU5cXFxcY29uZmlnXCIsXG4gIFwiZnJvbnRlbmRCdW5kbGVPdXRwdXRcIjogXCJGOlxcXFxQcm95ZWN0by1TZWd1cmlkYWRcXFxcZ2VzdGlvbi1zZWd1cmlkYWQtYWVyb3BvcnR1YXJpYVxcXFx0YXJnZXRcXFxcY2xhc3Nlc1xcXFxNRVRBLUlORlxcXFxWQUFESU5cXFxcd2ViYXBwXCIsXG4gIFwiZGV2QnVuZGxlT3V0cHV0XCI6IFwiRjovUHJveWVjdG8tU2VndXJpZGFkL2dlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWEvdGFyZ2V0L2Rldi1idW5kbGUvd2ViYXBwXCIsXG4gIFwiZGV2QnVuZGxlU3RhdHNPdXRwdXRcIjogXCJGOi9Qcm95ZWN0by1TZWd1cmlkYWQvZ2VzdGlvbi1zZWd1cmlkYWQtYWVyb3BvcnR1YXJpYS90YXJnZXQvZGV2LWJ1bmRsZS9jb25maWdcIixcbiAgXCJqYXJSZXNvdXJjZXNGb2xkZXJcIjogXCJGOi9Qcm95ZWN0by1TZWd1cmlkYWQvZ2VzdGlvbi1zZWd1cmlkYWQtYWVyb3BvcnR1YXJpYS8uL3NyYy9tYWluL2Zyb250ZW5kL2dlbmVyYXRlZC9qYXItcmVzb3VyY2VzXCIsXG4gIFwidGhlbWVOYW1lXCI6IFwiZ2VzdGlvbnNlZ3VyaWRhZGFlcm9wb3J0dWFyaWFcIixcbiAgXCJjbGllbnRTZXJ2aWNlV29ya2VyU291cmNlXCI6IFwiRjpcXFxcUHJveWVjdG8tU2VndXJpZGFkXFxcXGdlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWFcXFxcdGFyZ2V0XFxcXHN3LnRzXCIsXG4gIFwicHdhRW5hYmxlZFwiOiBmYWxzZSxcbiAgXCJvZmZsaW5lRW5hYmxlZFwiOiBmYWxzZSxcbiAgXCJvZmZsaW5lUGF0aFwiOiBcIidvZmZsaW5lLmh0bWwnXCJcbn0iLCAiY29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2Rpcm5hbWUgPSBcIkY6XFxcXFByb3llY3RvLVNlZ3VyaWRhZFxcXFxnZXN0aW9uLXNlZ3VyaWRhZC1hZXJvcG9ydHVhcmlhXFxcXHRhcmdldFxcXFxwbHVnaW5zXFxcXHJvbGx1cC1wbHVnaW4tcG9zdGNzcy1saXQtY3VzdG9tXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ZpbGVuYW1lID0gXCJGOlxcXFxQcm95ZWN0by1TZWd1cmlkYWRcXFxcZ2VzdGlvbi1zZWd1cmlkYWQtYWVyb3BvcnR1YXJpYVxcXFx0YXJnZXRcXFxccGx1Z2luc1xcXFxyb2xsdXAtcGx1Z2luLXBvc3Rjc3MtbGl0LWN1c3RvbVxcXFxyb2xsdXAtcGx1Z2luLXBvc3Rjc3MtbGl0LmpzXCI7Y29uc3QgX192aXRlX2luamVjdGVkX29yaWdpbmFsX2ltcG9ydF9tZXRhX3VybCA9IFwiZmlsZTovLy9GOi9Qcm95ZWN0by1TZWd1cmlkYWQvZ2VzdGlvbi1zZWd1cmlkYWQtYWVyb3BvcnR1YXJpYS90YXJnZXQvcGx1Z2lucy9yb2xsdXAtcGx1Z2luLXBvc3Rjc3MtbGl0LWN1c3RvbS9yb2xsdXAtcGx1Z2luLXBvc3Rjc3MtbGl0LmpzXCI7LyoqXG4gKiBNSVQgTGljZW5zZVxuXG5Db3B5cmlnaHQgKGMpIDIwMTkgVW1iZXJ0byBQZXBhdG9cblxuUGVybWlzc2lvbiBpcyBoZXJlYnkgZ3JhbnRlZCwgZnJlZSBvZiBjaGFyZ2UsIHRvIGFueSBwZXJzb24gb2J0YWluaW5nIGEgY29weVxub2YgdGhpcyBzb2Z0d2FyZSBhbmQgYXNzb2NpYXRlZCBkb2N1bWVudGF0aW9uIGZpbGVzICh0aGUgXCJTb2Z0d2FyZVwiKSwgdG8gZGVhbFxuaW4gdGhlIFNvZnR3YXJlIHdpdGhvdXQgcmVzdHJpY3Rpb24sIGluY2x1ZGluZyB3aXRob3V0IGxpbWl0YXRpb24gdGhlIHJpZ2h0c1xudG8gdXNlLCBjb3B5LCBtb2RpZnksIG1lcmdlLCBwdWJsaXNoLCBkaXN0cmlidXRlLCBzdWJsaWNlbnNlLCBhbmQvb3Igc2VsbFxuY29waWVzIG9mIHRoZSBTb2Z0d2FyZSwgYW5kIHRvIHBlcm1pdCBwZXJzb25zIHRvIHdob20gdGhlIFNvZnR3YXJlIGlzXG5mdXJuaXNoZWQgdG8gZG8gc28sIHN1YmplY3QgdG8gdGhlIGZvbGxvd2luZyBjb25kaXRpb25zOlxuXG5UaGUgYWJvdmUgY29weXJpZ2h0IG5vdGljZSBhbmQgdGhpcyBwZXJtaXNzaW9uIG5vdGljZSBzaGFsbCBiZSBpbmNsdWRlZCBpbiBhbGxcbmNvcGllcyBvciBzdWJzdGFudGlhbCBwb3J0aW9ucyBvZiB0aGUgU29mdHdhcmUuXG5cblRIRSBTT0ZUV0FSRSBJUyBQUk9WSURFRCBcIkFTIElTXCIsIFdJVEhPVVQgV0FSUkFOVFkgT0YgQU5ZIEtJTkQsIEVYUFJFU1MgT1JcbklNUExJRUQsIElOQ0xVRElORyBCVVQgTk9UIExJTUlURUQgVE8gVEhFIFdBUlJBTlRJRVMgT0YgTUVSQ0hBTlRBQklMSVRZLFxuRklUTkVTUyBGT1IgQSBQQVJUSUNVTEFSIFBVUlBPU0UgQU5EIE5PTklORlJJTkdFTUVOVC4gSU4gTk8gRVZFTlQgU0hBTEwgVEhFXG5BVVRIT1JTIE9SIENPUFlSSUdIVCBIT0xERVJTIEJFIExJQUJMRSBGT1IgQU5ZIENMQUlNLCBEQU1BR0VTIE9SIE9USEVSXG5MSUFCSUxJVFksIFdIRVRIRVIgSU4gQU4gQUNUSU9OIE9GIENPTlRSQUNULCBUT1JUIE9SIE9USEVSV0lTRSwgQVJJU0lORyBGUk9NLFxuT1VUIE9GIE9SIElOIENPTk5FQ1RJT04gV0lUSCBUSEUgU09GVFdBUkUgT1IgVEhFIFVTRSBPUiBPVEhFUiBERUFMSU5HUyBJTiBUSEVcblNPRlRXQVJFLlxuICovXG4vLyBUaGlzIGlzIGh0dHBzOi8vZ2l0aHViLmNvbS91bWJvcGVwYXRvL3JvbGx1cC1wbHVnaW4tcG9zdGNzcy1saXQgMi4wLjAgKyBodHRwczovL2dpdGh1Yi5jb20vdW1ib3BlcGF0by9yb2xsdXAtcGx1Z2luLXBvc3Rjc3MtbGl0L3B1bGwvNTRcbi8vIHRvIG1ha2UgaXQgd29yayB3aXRoIFZpdGUgM1xuLy8gT25jZSAvIGlmIGh0dHBzOi8vZ2l0aHViLmNvbS91bWJvcGVwYXRvL3JvbGx1cC1wbHVnaW4tcG9zdGNzcy1saXQvcHVsbC81NCBpcyBtZXJnZWQgdGhpcyBzaG91bGQgYmUgcmVtb3ZlZCBhbmQgcm9sbHVwLXBsdWdpbi1wb3N0Y3NzLWxpdCBzaG91bGQgYmUgdXNlZCBpbnN0ZWFkXG5cbmltcG9ydCB7IGNyZWF0ZUZpbHRlciB9IGZyb20gJ0Byb2xsdXAvcGx1Z2ludXRpbHMnO1xuaW1wb3J0IHRyYW5zZm9ybUFzdCBmcm9tICd0cmFuc2Zvcm0tYXN0JztcblxuY29uc3QgYXNzZXRVcmxSRSA9IC9fX1ZJVEVfQVNTRVRfXyhbXFx3JF0rKV9fKD86XFwkXyguKj8pX18pPy9nXG5cbmNvbnN0IGVzY2FwZSA9IChzdHIpID0+XG4gIHN0clxuICAgIC5yZXBsYWNlKGFzc2V0VXJsUkUsICcke3Vuc2FmZUNTU1RhZyhcIl9fVklURV9BU1NFVF9fJDFfXyQyXCIpfScpXG4gICAgLnJlcGxhY2UoL2AvZywgJ1xcXFxgJylcbiAgICAucmVwbGFjZSgvXFxcXCg/IWApL2csICdcXFxcXFxcXCcpO1xuXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbiBwb3N0Y3NzTGl0KG9wdGlvbnMgPSB7fSkge1xuICBjb25zdCBkZWZhdWx0T3B0aW9ucyA9IHtcbiAgICBpbmNsdWRlOiAnKiovKi57Y3NzLHNzcyxwY3NzLHN0eWwsc3R5bHVzLHNhc3Msc2NzcyxsZXNzfScsXG4gICAgZXhjbHVkZTogbnVsbCxcbiAgICBpbXBvcnRQYWNrYWdlOiAnbGl0J1xuICB9O1xuXG4gIGNvbnN0IG9wdHMgPSB7IC4uLmRlZmF1bHRPcHRpb25zLCAuLi5vcHRpb25zIH07XG4gIGNvbnN0IGZpbHRlciA9IGNyZWF0ZUZpbHRlcihvcHRzLmluY2x1ZGUsIG9wdHMuZXhjbHVkZSk7XG5cbiAgcmV0dXJuIHtcbiAgICBuYW1lOiAncG9zdGNzcy1saXQnLFxuICAgIGVuZm9yY2U6ICdwb3N0JyxcbiAgICB0cmFuc2Zvcm0oY29kZSwgaWQpIHtcbiAgICAgIGlmICghZmlsdGVyKGlkKSkgcmV0dXJuO1xuICAgICAgY29uc3QgYXN0ID0gdGhpcy5wYXJzZShjb2RlLCB7fSk7XG4gICAgICAvLyBleHBvcnQgZGVmYXVsdCBjb25zdCBjc3M7XG4gICAgICBsZXQgZGVmYXVsdEV4cG9ydE5hbWU7XG5cbiAgICAgIC8vIGV4cG9ydCBkZWZhdWx0ICcuLi4nO1xuICAgICAgbGV0IGlzRGVjbGFyYXRpb25MaXRlcmFsID0gZmFsc2U7XG4gICAgICBjb25zdCBtYWdpY1N0cmluZyA9IHRyYW5zZm9ybUFzdChjb2RlLCB7IGFzdDogYXN0IH0sIChub2RlKSA9PiB7XG4gICAgICAgIGlmIChub2RlLnR5cGUgPT09ICdFeHBvcnREZWZhdWx0RGVjbGFyYXRpb24nKSB7XG4gICAgICAgICAgZGVmYXVsdEV4cG9ydE5hbWUgPSBub2RlLmRlY2xhcmF0aW9uLm5hbWU7XG5cbiAgICAgICAgICBpc0RlY2xhcmF0aW9uTGl0ZXJhbCA9IG5vZGUuZGVjbGFyYXRpb24udHlwZSA9PT0gJ0xpdGVyYWwnO1xuICAgICAgICB9XG4gICAgICB9KTtcblxuICAgICAgaWYgKCFkZWZhdWx0RXhwb3J0TmFtZSAmJiAhaXNEZWNsYXJhdGlvbkxpdGVyYWwpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuICAgICAgbWFnaWNTdHJpbmcud2Fsaygobm9kZSkgPT4ge1xuICAgICAgICBpZiAoZGVmYXVsdEV4cG9ydE5hbWUgJiYgbm9kZS50eXBlID09PSAnVmFyaWFibGVEZWNsYXJhdGlvbicpIHtcbiAgICAgICAgICBjb25zdCBleHBvcnRlZFZhciA9IG5vZGUuZGVjbGFyYXRpb25zLmZpbmQoKGQpID0+IGQuaWQubmFtZSA9PT0gZGVmYXVsdEV4cG9ydE5hbWUpO1xuICAgICAgICAgIGlmIChleHBvcnRlZFZhcikge1xuICAgICAgICAgICAgZXhwb3J0ZWRWYXIuaW5pdC5lZGl0LnVwZGF0ZShgY3NzVGFnXFxgJHtlc2NhcGUoZXhwb3J0ZWRWYXIuaW5pdC52YWx1ZSl9XFxgYCk7XG4gICAgICAgICAgfVxuICAgICAgICB9XG5cbiAgICAgICAgaWYgKGlzRGVjbGFyYXRpb25MaXRlcmFsICYmIG5vZGUudHlwZSA9PT0gJ0V4cG9ydERlZmF1bHREZWNsYXJhdGlvbicpIHtcbiAgICAgICAgICBub2RlLmRlY2xhcmF0aW9uLmVkaXQudXBkYXRlKGBjc3NUYWdcXGAke2VzY2FwZShub2RlLmRlY2xhcmF0aW9uLnZhbHVlKX1cXGBgKTtcbiAgICAgICAgfVxuICAgICAgfSk7XG4gICAgICBtYWdpY1N0cmluZy5wcmVwZW5kKGBpbXBvcnQge2NzcyBhcyBjc3NUYWcsIHVuc2FmZUNTUyBhcyB1bnNhZmVDU1NUYWd9IGZyb20gJyR7b3B0cy5pbXBvcnRQYWNrYWdlfSc7XFxuYCk7XG4gICAgICByZXR1cm4ge1xuICAgICAgICBjb2RlOiBtYWdpY1N0cmluZy50b1N0cmluZygpLFxuICAgICAgICBtYXA6IG1hZ2ljU3RyaW5nLmdlbmVyYXRlTWFwKHtcbiAgICAgICAgICBoaXJlczogdHJ1ZVxuICAgICAgICB9KVxuICAgICAgfTtcbiAgICB9XG4gIH07XG59O1xuIiwgImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJGOlxcXFxQcm95ZWN0by1TZWd1cmlkYWRcXFxcZ2VzdGlvbi1zZWd1cmlkYWQtYWVyb3BvcnR1YXJpYVwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiRjpcXFxcUHJveWVjdG8tU2VndXJpZGFkXFxcXGdlc3Rpb24tc2VndXJpZGFkLWFlcm9wb3J0dWFyaWFcXFxcdml0ZS5jb25maWcudHNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0Y6L1Byb3llY3RvLVNlZ3VyaWRhZC9nZXN0aW9uLXNlZ3VyaWRhZC1hZXJvcG9ydHVhcmlhL3ZpdGUuY29uZmlnLnRzXCI7aW1wb3J0IHsgVXNlckNvbmZpZ0ZuIH0gZnJvbSAndml0ZSc7XG5pbXBvcnQgeyBvdmVycmlkZVZhYWRpbkNvbmZpZyB9IGZyb20gJy4vdml0ZS5nZW5lcmF0ZWQnO1xuXG5jb25zdCBjdXN0b21Db25maWc6IFVzZXJDb25maWdGbiA9IChlbnYpID0+ICh7XG4gIC8vIEhlcmUgeW91IGNhbiBhZGQgY3VzdG9tIFZpdGUgcGFyYW1ldGVyc1xuICAvLyBodHRwczovL3ZpdGVqcy5kZXYvY29uZmlnL1xufSk7XG5cbmV4cG9ydCBkZWZhdWx0IG92ZXJyaWRlVmFhZGluQ29uZmlnKGN1c3RvbUNvbmZpZyk7XG4iXSwKICAibWFwcGluZ3MiOiAiO0FBTUEsT0FBTyxVQUFVO0FBQ2pCLFNBQVMsY0FBQUEsYUFBWSxhQUFBQyxZQUFXLGVBQUFDLGNBQWEsZ0JBQUFDLGVBQWMsaUJBQUFDLHNCQUFxQjtBQUNoRixTQUFTLGtCQUFrQjtBQUMzQixZQUFZLFNBQVM7OztBQ1dyQixTQUFTLGNBQUFDLGFBQVksZ0JBQUFDLHFCQUFvQjtBQUN6QyxTQUFTLFdBQUFDLGdC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NkI7QUFBQSxnQkFDM0QsVUFBVTtBQUFBLGNBQ1osQ0FBQztBQUFBLFlBQ0g7QUFDQSxvQkFBUSxLQUFLO0FBQUEsY0FDWCxLQUFLO0FBQUEsY0FDTCxPQUFPLEVBQUUsTUFBTSxVQUFVLEtBQUssdUJBQXVCO0FBQUEsY0FDckQsVUFBVTtBQUFBLFlBQ1osQ0FBQztBQUNELG1CQUFPO0FBQUEsVUFDVDtBQUFBLFFBQ0Y7QUFBQSxNQUNGO0FBQUEsTUFDQSxRQUFRO0FBQUEsUUFDTixZQUFZO0FBQUEsTUFDZCxDQUFDO0FBQUEsTUFDRCxrQkFBa0IsV0FBVyxFQUFFLFlBQVksTUFBTSxVQUFVLGVBQWUsQ0FBQztBQUFBLElBRTdFO0FBQUEsRUFDRjtBQUNGO0FBRU8sSUFBTSx1QkFBdUIsQ0FBQ0Msa0JBQStCO0FBQ2xFLFNBQU8sYUFBYSxDQUFDLFFBQVEsWUFBWSxhQUFhLEdBQUcsR0FBR0EsY0FBYSxHQUFHLENBQUMsQ0FBQztBQUNoRjtBQUNBLFNBQVMsV0FBVyxRQUF3QjtBQUMxQyxRQUFNLGNBQWMsS0FBSyxRQUFRLG1CQUFtQixRQUFRLGNBQWM7QUFDMUUsU0FBTyxLQUFLLE1BQU1SLGNBQWEsYUFBYSxFQUFFLFVBQVUsUUFBUSxDQUFDLENBQUMsRUFBRTtBQUN0RTtBQUNBLFNBQVMsWUFBWSxRQUF3QjtBQUMzQyxRQUFNLGNBQWMsS0FBSyxRQUFRLG1CQUFtQixRQUFRLGNBQWM7QUFDMUUsU0FBTyxLQUFLLE1BQU1BLGNBQWEsYUFBYSxFQUFFLFVBQVUsUUFBUSxDQUFDLENBQUMsRUFBRTtBQUN0RTs7O0FRLzFCQSxJQUFNLGVBQTZCLENBQUMsU0FBUztBQUFBO0FBQUE7QUFHN0M7QUFFQSxJQUFPLHNCQUFRLHFCQUFxQixZQUFZOyIsCiAgIm5hbWVzIjogWyJleGlzdHNTeW5jIiwgIm1rZGlyU3luYyIsICJyZWFkZGlyU3luYyIsICJyZWFkRmlsZVN5bmMiLCAid3JpdGVGaWxlU3luYyIsICJleGlzdHNTeW5jIiwgInJlYWRGaWxlU3luYyIsICJyZXNvbHZlIiwgImdsb2JTeW5jIiwgInJlc29sdmUiLCAiYmFzZW5hbWUiLCAiZXhpc3RzU3luYyIsICJ0aGVtZUZvbGRlciIsICJ0aGVtZUZvbGRlciIsICJyZXNvbHZlIiwgImdsb2JTeW5jIiwgImV4aXN0c1N5bmMiLCAiYmFzZW5hbWUiLCAidmFyaWFibGUiLCAiZmlsZW5hbWUiLCAiZXhpc3RzU3luYyIsICJyZXNvbHZlIiwgInRoZW1lRm9sZGVyIiwgInJlYWRGaWxlU3luYyIsICJleGlzdHNTeW5jIiwgInJlYWRGaWxlU3luYyIsICJyZXNvbHZlIiwgImJhc2VuYW1lIiwgImdsb2JTeW5jIiwgInRoZW1lRm9sZGVyIiwgImdldFRoZW1lUHJvcGVydGllcyIsICJnbG9iU3luYyIsICJyZXNvbHZlIiwgImV4aXN0c1N5bmMiLCAicmVhZEZpbGVTeW5jIiwgInJlcGxhY2UiLCAiYmFzZW5hbWUiLCAicGF0aCIsICJyZXF1aXJlIiwgImV4aXN0c1N5bmMiLCAicmVhZEZpbGVTeW5jIiwgIm1rZGlyU3luYyIsICJidW5kbGUiLCAicmVhZGRpclN5bmMiLCAidGhlbWVGb2xkZXIiLCAid3JpdGVGaWxlU3luYyIsICJlIiwgInBhdGgiLCAiY3VzdG9tQ29uZmlnIl0KfQo=
