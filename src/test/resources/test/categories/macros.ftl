<#ftl encoding="UTF-8" />
<#---
    Global comment for categories test file.
    This is the global comment that should appear at the top of the documentation.
    @author Test Author
-->
<#-- @begin Menu macros -->
<#---
    Creates a main menu.
    @param {Sequence} items Menu items
-->
<#macro mainMenu items>
    <ul><#list items as item><li>${item}</li></#list></ul>
</#macro>

<#---
    Creates a sub menu.
    @param {Sequence} items Sub menu items
-->
<#macro subMenu items>
    <ul class="submenu"><#list items as item><li>${item}</li></#list></ul>
</#macro>
<#-- @end -->

<#-- @begin Form macros -->
<#---
    Creates a text input field.
    @param {String} name Field name
    @param {String} [value=""] Default value
-->
<#macro textInput name value="">
    <input type="text" name="${name}" value="${value}">
</#macro>

<#---
    Creates a submit button.
    @param {String} label Button label
-->
<#macro submitButton label>
    <button type="submit">${label}</button>
</#macro>
<#-- @end -->
