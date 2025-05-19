import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/login/theme/lumo/vaadin-login-form.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/combo-box/theme/lumo/vaadin-combo-box.js';
import 'Frontend/generated/jar-resources/comboBoxConnector.js';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column.js';
import '@vaadin/email-field/theme/lumo/vaadin-email-field.js';
import '@vaadin/app-layout/theme/lumo/vaadin-app-layout.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/icon/theme/lumo/vaadin-icon.js';
import '@vaadin/upload/theme/lumo/vaadin-upload.js';
import '@vaadin/context-menu/theme/lumo/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-item.js';
import '@vaadin/multi-select-combo-box/theme/lumo/vaadin-multi-select-combo-box.js';
import '@vaadin/grid/theme/lumo/vaadin-grid.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-sorter.js';
import '@vaadin/checkbox/theme/lumo/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import 'Frontend/generated/jar-resources/buttonFunctions.js';
import '@vaadin/split-layout/theme/lumo/vaadin-split-layout.js';
import '@vaadin/checkbox-group/theme/lumo/vaadin-checkbox-group.js';
import '@vaadin/text-field/theme/lumo/vaadin-text-field.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/date-picker/theme/lumo/vaadin-date-picker.js';
import 'Frontend/generated/jar-resources/datepickerConnector.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-layout.js';
import '@vaadin/app-layout/theme/lumo/vaadin-drawer-toggle.js';
import '@vaadin/horizontal-layout/theme/lumo/vaadin-horizontal-layout.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column-group.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '9df29b913238da2f682fb6a196e879443ecb4bf49948840163e07ba68f54fbb6') {
    pending.push(import('./chunks/chunk-2ee6b7ab7e3680a72bc7a10538c4b6b937e26306139fcf4e359c41a244878d55.js'));
  }
  if (key === 'df3cb2886cb4b5366bd55b28ae47c9cd902eff28288d87626823a8dcbd2fc880') {
    pending.push(import('./chunks/chunk-47dd70a5974bd90a406fb3069fbe6e87312a8bb8bf959ca89ed233ead05df630.js'));
  }
  if (key === 'c3bb3eeb2ace092c3fa2e0976b53a800b0aa8caa2495872278cd3610572ac497') {
    pending.push(import('./chunks/chunk-41238b7e86946093d6bcf0c380c8eb3c14975645bc1f0e54520711e70a6618ce.js'));
  }
  if (key === '7d1ee31d90c8a5082929a7d36cfb9a7254faa4858aa01f232928f85938f2a310') {
    pending.push(import('./chunks/chunk-2ee6b7ab7e3680a72bc7a10538c4b6b937e26306139fcf4e359c41a244878d55.js'));
  }
  if (key === 'ad9a42924c3d2d08a69e8f5bd1e0a486be2bd2caff2b918d1597fd2dd0576b04') {
    pending.push(import('./chunks/chunk-47dd70a5974bd90a406fb3069fbe6e87312a8bb8bf959ca89ed233ead05df630.js'));
  }
  if (key === 'a1b8642784ee46808971b1056125e0f6cbea75f4c672d8309c5d58685ff6779e') {
    pending.push(import('./chunks/chunk-47dd70a5974bd90a406fb3069fbe6e87312a8bb8bf959ca89ed233ead05df630.js'));
  }
  if (key === 'c839ee7cf5746c4331e352e696adcece06a612b18a95f3a7d46b7d4d8b0f202f') {
    pending.push(import('./chunks/chunk-a99d5a86387f3ef7d3ef80da10a4fcdfdb0fdf72663b019f932e7a404927c8ba.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}