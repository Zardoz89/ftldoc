<#ftl encoding="ISO-8859-1" />
<#---
    And FTL lib file
    @copyright bla bla bla 2021
-->

<#-- @begin Auxiliar functions -->

<#---
    Function that emulates C/Java '?' operator
    @param condition {Boolean} Expression that evaluates on a <code>boolean</code> value
-->
<#function iif condition value1 value2>
    <#if condition>
        <#return value1 />
    <#else>
        <#return value2 />
    </#if>
</#function>

<#---
    Function that generates a string from a sequence, putting a separator bettwen elements
    @param seq {Sequence} Sequence to be converted to String
    @param {String} [separator=" "]  Seperator element.
-->
<#function join seq separator = " ">
    <#local res = "" />
    <#list seq as e>
        <#if e_index == 0>
            <#local res = res + e />
        <#else>
            <#local res = res + separator + e />
        </#if>
    </#list>
    <#return res />
</#function>

<#---
    Function that converts a value to a Boolean
    @param {String|Number|Boolean|Object} input String or Number to be converted to Boolean
    @return Boolean value being true or false
-->
<#function toBool input>
    <#if input?is_boolean>
        <#return input />
    </#if>

    <#-- Number -->
    <#if input?is_number>
        <#if input != 0>
            <#return true />
        <#else>
            <#return false />
        </#if>
    </#if>

    <#-- String -->
    <#if input?is_string>
        <#if input == "" || input == "false"|| input == "not"  || input == "no" || input == "n" || input == "f" >
            <#return false />
        <#else>
            <#return true />
        </#if>
    </#if>
    
    <#-- != Null or not has content -->
    <#if input?? && input?has_content>
        <#return true />
    <#else>
        <#return false />
    </#if>
</#function>

<#---
    Checks if a String value it's a number
-->
<#function isStringNumber variable>
    <#attempt>
        <#assign numberVariable = variable?number />
        <#return numberVariable?is_number />
    <#recover>
        <#return false />
    </#attempt>
</#function>

<#-- @end -->