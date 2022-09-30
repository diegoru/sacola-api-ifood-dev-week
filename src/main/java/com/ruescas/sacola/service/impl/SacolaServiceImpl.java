package com.ruescas.sacola.service.impl;

import com.ruescas.sacola.enumeration.FormaPagamento;
import com.ruescas.sacola.model.Item;
import com.ruescas.sacola.model.Restaurante;
import com.ruescas.sacola.model.Sacola;
import com.ruescas.sacola.repository.ItemRepository;
import com.ruescas.sacola.repository.ProdutoRepository;
import com.ruescas.sacola.repository.SacolaRepository;
import com.ruescas.sacola.resource.dto.ItemDto;
import com.ruescas.sacola.service.SacolaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SacolaServiceImpl implements SacolaService {
    private final SacolaRepository sacolaRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemRepository itemRepository;

    @Override
    public Item incluirItemNaSacola(ItemDto itemDto) {
        Sacola sacola = verSacola(itemDto.getSacolaId());
        if (sacola.isFechada()) {
            throw new RuntimeException("Essa sacola está fechada!");
        }
        Item itemParaSerInserido = Item.builder()
                .quantidade(itemDto.getQuantidade())
                .sacola(sacola)
                .produto(produtoRepository.findById(itemDto.getProdutoId()).orElseThrow(
                        () -> {
                            throw new RuntimeException("Produto não encontrado!");
                        }
                ))
                .build();

        List<Item> itensDaSacola = sacola.getItens();
        if (itensDaSacola.isEmpty()) {
            itensDaSacola.add(itemParaSerInserido);
        } else {
            Restaurante restauranteAtual = itensDaSacola.get(0).getProduto().getRestaurante();
            Restaurante restauranteDoItemParaAdicionar = itemParaSerInserido.getProduto().getRestaurante();
            if (restauranteAtual.equals(restauranteDoItemParaAdicionar)) {
                itensDaSacola.add(itemParaSerInserido);
            } else {
                throw new RuntimeException("Não é possível adicionar produtos de restaurantes diferentes.");
            }
        }

        List<Double> valorDosItens = new ArrayList<>();
        for (Item item : itensDaSacola) {
            double valorTotalItem = item.getProduto().getValorUnitario() * item.getQuantidade();
            valorDosItens.add(valorTotalItem);
        }

        Double valorTotalsacola = 0.0;
        for (Double valorItens : valorDosItens) {
            valorTotalsacola += valorItens;
        }

        sacola.setValorTotal(valorTotalsacola);
        sacolaRepository.save(sacola);
        return itemParaSerInserido;
    }

    @Override
    public Sacola verSacola(Long id) {
        return sacolaRepository.findById(id).orElseThrow(
                () -> {
                    throw new RuntimeException("Essa sacola não existe!");
                }
        );
    }

    @Override
    public Sacola fecharSacola(Long id, int numeroFormaPagamento) {
        Sacola sacola = verSacola(id);
        if (sacola.getItens().isEmpty()) {
            throw new RuntimeException("Inclua itens na sacola!");
        }
        FormaPagamento formaPagamento =
                numeroFormaPagamento == 0 ? FormaPagamento.DINHEIRO : FormaPagamento.MAQUINETA;
        sacola.setFormaPagamento(formaPagamento);
        sacola.setFechada(true);
        return sacolaRepository.save(sacola);
    }
}
