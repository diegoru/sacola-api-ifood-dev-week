package com.ruescas.sacola.service;

import com.ruescas.sacola.model.Item;
import com.ruescas.sacola.model.Sacola;
import com.ruescas.sacola.resource.dto.ItemDto;

public interface SacolaService {
    Item incluirItemNaSacola(ItemDto itemDto);
    Sacola verSacola(Long id);
    Sacola fecharSacola(Long id, int formaPagamento);

}
