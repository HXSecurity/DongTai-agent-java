---
layout: post
title: Agent 功能
---
# Agent 功能

Dongtai Java Agent 是洞态 Iast 为 Java 应用提供的数据采集工具，在添加了 Dongtai Java Agent 的 Java 应用程序中，Agent 会收集数据向 DongTai OpenAPI 服务上报。

## API 漏洞检测

Dongtai Java Agent  会通过改写基于云端规则的类字节码，每当有 http 请求产生时，Agent 会采集所需的数据，然后将数据发送到 Dongtai OpenAPI 服务，再由 DongTai Engine 处理这些数据以确定本次http请求访问的接口是否存在安全漏洞。 

## 第三方组件漏洞检测

Dongtai Java Agent 会收集应用所有依赖的第三方组件信息，上报至 Dongtai OpenAPI 服务，然后经过洞态漏洞库的匹配确定应用是否在使用含有漏洞的第三方组件。

## API 导航

DongTai Java Agent 会收集应用的所有接口信息，包括接口访问路径、接口类型、接口方法所接收的参数类型、接口方法的返回值、接口方法的注解等，然后上报 至 Dongtai OpenAPI 服务进行梳理，生成完整的 API 导航。可直接在 API 导航中发送 API 请求、自动测试等。

## 主动验证

当 DongTai Engine 检测出 Java 应用某接口有安全漏洞后，会向 DongTai Java Agent 发送重放请求，这个重放请求会携带特定的 payload ，去验证该漏洞是否真实存在。
