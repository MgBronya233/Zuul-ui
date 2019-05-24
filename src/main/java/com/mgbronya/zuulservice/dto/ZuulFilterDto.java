package com.mgbronya.zuulservice.dto;

import lombok.Data;

@Data
public class ZuulFilterDto {

    String filterName;

    String filterType;

    Integer filterOrder;

    Boolean shouldFilter;

}
